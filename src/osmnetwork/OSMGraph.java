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
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.graph.DirectedMultigraph;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author demory
 */
public class OSMGraph extends DirectedMultigraph<OSMNode, OSMWay> {
  
  private Map<Long, OSMNode> nodes_ = new HashMap<Long, OSMNode>();
  private Map<Long, OSMWay> ways_ = new HashMap<Long, OSMWay>();
  
  public OSMGraph() {
    super(OSMWay.class);
    
  }
  
  public void addNode(OSMNode node) {
    nodes_.put(node.getID(), node);
  }
  
  public void addWay(OSMWay way) {
    OSMNode fromNode = nodes_.get(way.getFirstNodeID());
    OSMNode toNode = nodes_.get(way.getLastNodeID());
    if(!containsVertex(fromNode)) addVertex(fromNode);
    if(!containsVertex(toNode)) addVertex(toNode);
    addEdge(fromNode, toNode, way);    
    ways_.put(way.getID(), way);
  }
  
  long lastId=1;
  public void mergeEdges() {
    Set<OSMNode> nodes = new HashSet<OSMNode>(vertexSet());
    while(!nodes.isEmpty()) {
      OSMNode node = nodes.iterator().next();
      if(inDegreeOf(node)+outDegreeOf(node) == 2) {
        OSMWay way1=null, way2=null, newWay=null;
        if(inDegreeOf(node) == outDegreeOf(node)) {
          way1 = incomingEdgesOf(node).iterator().next();
          way2 = outgoingEdgesOf(node).iterator().next();
          way2.getNodeIDs().remove(0);
          way1.getNodeIDs().addAll(way2.getNodeIDs());
          newWay = new OSMWay(way1.getID());
          addEdge(getEdgeSource(way1), getEdgeTarget(way2), newWay);
        }
        else if(inDegreeOf(node) == 2) {
          Iterator<OSMWay> iter = incomingEdgesOf(node).iterator();
          way1 = iter.next();
          way2 = iter.next();
          Collections.reverse(way2.getNodeIDs());
          way2.getNodeIDs().remove(0);
          way1.getNodeIDs().addAll(way2.getNodeIDs());
          newWay = new OSMWay(way1.getID());
          addEdge(getEdgeSource(way1), getEdgeSource(way2), newWay);        
        }
        else if(outDegreeOf(node) == 2) {
          Iterator<OSMWay> iter = outgoingEdgesOf(node).iterator();
          way1 = iter.next();
          way2 = iter.next();
          Collections.reverse(way1.getNodeIDs());
          way2.getNodeIDs().remove(0);
          way1.getNodeIDs().addAll(way2.getNodeIDs());
          newWay = new OSMWay(way1.getID());
          addEdge(getEdgeTarget(way1), getEdgeTarget(way2), newWay);//));*/
        }
        for(Long id : way1.getNodeIDs()) {
          newWay.addNodeID(id);
        }
        ways_.remove(way1.getID());
        ways_.remove(way2.getID());
        ways_.put(newWay.getID(), newWay);
        removeVertex(node);
      }
      nodes.remove(node);
    }
  }
  
  public void writeJSON(String filename) {
    JSONObject json = new JSONObject();
    JSONArray features = new JSONArray();
    System.out.println("json ways: "+ways_.size());
    for(OSMWay way : ways_.values()) {
      JSONObject feature = new JSONObject();
      feature.put("type", "Feature");
      feature.put("id", way.getID());
      JSONObject geom = new JSONObject();
      geom.put("type", "LineString");
      JSONArray coords = new JSONArray();        
      for(Long nodeId : way.getNodeIDs()) {
        JSONArray coord = new JSONArray();
        OSMNode node = nodes_.get(nodeId);
        if(node==null) {
          System.out.println("NULL!! nodeId="+nodeId+" way="+way.getID());
        }
        coord.add(node.getLon());
        coord.add(node.getLat());
        coords.add(coord);
      }
      geom.put("coordinates", coords);
      feature.put("geometry", geom);
      features.add(feature);
    }
    json.put("type", "FeatureCollection");
    json.put("features", features);

    try {
      FileWriter jsonWriter = new FileWriter(new File(filename));
      jsonWriter.write(json.toJSONString());
      jsonWriter.close();    
    } catch (IOException ex) {
      Logger.getLogger(OSMGraph.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public void writeCSV(String filename) {
    try {
      long largestID = 0;
      FileWriter csvWriter = new FileWriter(new File(filename));
      for(OSMWay way : ways_.values()) {
        largestID = Math.max(largestID, way.getID());
        csvWriter.write(""+way.getID());
        for(Long nodeId : way.getNodeIDs()) {
          OSMNode node = nodes_.get(nodeId);
          csvWriter.write(","+node.getLon()+","+node.getLat());
        }        
        csvWriter.write("\n");
      }
      csvWriter.close();
      System.out.println("largest: "+largestID);
    } catch (IOException ex) {
      Logger.getLogger(OSMGraph.class.getName()).log(Level.SEVERE, null, ex);
    }    
  }

}
