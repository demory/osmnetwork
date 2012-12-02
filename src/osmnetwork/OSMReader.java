/*  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package osmnetwork;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author demory
 */
public class OSMReader {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    // TODO code application logic here
    new OSMReader(args[0], args[1], args[2]);
  }

  private long lastId_ = 1000;
  
  private Map<Long, OSMNode> nodes_ = new HashMap<Long, OSMNode>();
  private Map<Long, Integer> nodeVisitCount_ = new HashMap<Long, Integer>();
  private Set<Long> splitNodes_ = new HashSet<Long>();
  private Set<OSMWay> splitWays_ = new HashSet<OSMWay>();
  
  String[] validHighwayTypes_ = { "motorway", "motorway_link", "trunk", "trunk_link", "primary", "secondary", "tertiary", "residential" };
  
  public OSMReader(String osmFile, String jsonOutFile, String csvOutFile) {    
    System.out.println("reading");
    readFromXML(osmFile, jsonOutFile, csvOutFile);
  }
  
  public void readFromXML(String osmFile, String jsonOutFile, String csvOutFile) {
    
    try {
      DocumentBuilder docBuilder;
      docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

      Document doc = docBuilder.parse(new File(osmFile));
      NodeList docNodes = doc.getChildNodes();
      
      NodeList osmNodes = docNodes.item(0).getChildNodes();
      
      System.out.println("osmnodes: "+osmNodes.getLength());

      Set<OSMWay> unsplitWays = new HashSet<OSMWay>();

      Set<String> validHighwayTypes = new HashSet<String>(Arrays.asList(validHighwayTypes_));
      
      for (int i = 0; i < osmNodes.getLength(); i++) {
        Node n = osmNodes.item(i);
        if(n.getNodeName().equals("node")) {
          Long id = new Long(n.getAttributes().getNamedItem("id").getNodeValue());
          Double lat = new Double(n.getAttributes().getNamedItem("lat").getNodeValue());
          Double lon = new Double(n.getAttributes().getNamedItem("lon").getNodeValue());
          nodes_.put(id, new OSMNode(id, lon, lat)); //new Point2D.Double(lon, lat));
        }
        if(n.getNodeName().equals("way")) {
          Long id = new Long(n.getAttributes().getNamedItem("id").getNodeValue());
          OSMWay way = new OSMWay(id);
          NodeList wayNodes = n.getChildNodes();
          
          // check validity
          int nodeCount = 0;
          for(int j = 0; j < wayNodes.getLength(); j++) {
            Node wn = wayNodes.item(j);
            if(wn.getNodeName().equals("nd")) nodeCount++; 
            if(wn.getNodeName().equals("tag")) {
              if(wn.getAttributes().getNamedItem("k").getNodeValue().equals("highway")) {
                //System.out.println(wn.getAttributes().getNamedItem("v").getNodeValue());
                way.setType(wn.getAttributes().getNamedItem("v").getNodeValue());
              }
            }
          }
          if(nodeCount == 0 || !validHighwayTypes.contains(way.getType())) {
            continue;
          }
          
          for(int j = 0; j < wayNodes.getLength(); j++) {
            Node wn = wayNodes.item(j);
            if(wn.getNodeName().equals("tag")) {
              if(wn.getAttributes().getNamedItem("k").getNodeValue().equals("name")) {
                way.setName(wn.getAttributes().getNamedItem("v").getNodeValue());
              }
            }
            if(wn.getNodeName().equals("nd")) {
              Long nd = new Long(wn.getAttributes().getNamedItem("ref").getNodeValue());
              way.addNodeID(nd);
              
              if(nodeVisitCount_.containsKey(nd)) {
                int newCount = nodeVisitCount_.get(nd)+1;
                nodeVisitCount_.put(nd, newCount);
                if(newCount >= 2) splitNodes_.add(nd);
              }
              else {
                nodeVisitCount_.put(nd, 1);
              }
            }

          }
          unsplitWays.add(way);
        }
      }
      
      System.out.println("total nodes: "+nodes_.size());
      System.out.println("split nodes: "+splitNodes_.size());
      System.out.println("unsplit ways: "+unsplitWays.size());
      
      // split the ways
      
      for(OSMWay way : unsplitWays) {
        int subId=1;
        OSMWay currentWay = new OSMWay(lastId_++);//way.id_*1000+subId);
        currentWay.addNodeID(way.getFirstNodeID());
        for(int i=1; i<way.getNodeIDCount()-1; i++) {
          currentWay.addNodeID(way.getNodeID(i));
          if(splitNodes_.contains(way.getNodeID(i))) {
            splitWays_.add(currentWay);          
            subId++;
            currentWay = new OSMWay(lastId_++);//way.id_*1000+subId);
            currentWay.addNodeID(way.getNodeID(i));          
          }
        }
        currentWay.addNodeID(way.getLastNodeID());
        splitWays_.add(currentWay);
      }
      System.out.println("split ways: "+splitWays_.size());
      
      // construct the graph
      
      OSMGraph graph = new OSMGraph();

      for(Long nodeId : nodeVisitCount_.keySet()) {
        graph.addNode(nodes_.get(nodeId));
      }
      for(OSMWay way : splitWays_) graph.addWay(way);
      
      
      graph.mergeEdges();
      
      
      Set<OSMWay> exportWays = splitWays_; 
      
      // write the GeoJSON file
      graph.writeJSON(jsonOutFile);
      
      // write the CSV file
      graph.writeCSV(csvOutFile);
      
    } catch (Exception ex) {
      Logger.getLogger(OSMReader.class.getName()).log(Level.SEVERE, null, ex);
    }
    
  }
  
}
