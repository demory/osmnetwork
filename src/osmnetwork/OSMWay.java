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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author demory
 */
public class OSMWay {
  
  private Long id_;
  private String name_ = "Unnamed";
  private List<Long> nodeIds_ = new ArrayList<Long>();
  private String type_;

  public OSMWay(Long id) {
    id_ = id;
  }
  
  public Long getID() {
    return id_;
  }
  
  public void setType(String type) {
    type_ = type;
  }
  
  public String getType() {
    return type_;
  }
  
  public void setName(String name) {
    name_ = name;
  }
  
  public String getName() {
    return name_;
  }
  
  public List<Long> getNodeIDs() {
    return nodeIds_;
  }
  
  public void addNodeID(Long id) {
    nodeIds_.add(id);
  }
  
  public Long getNodeID(int i) {
    return nodeIds_.get(i);
  }
  
  public long getFirstNodeID() {
    return nodeIds_.get(0);
  }
  
  public int getNodeIDCount() {
    return nodeIds_.size();
  }
  
  public long getLastNodeID() {
    return nodeIds_.get(nodeIds_.size()-1);
  }
  
  public String toString() {
    return id_+" ("+name_+") nodes="+nodeIds_.size();
  }
  
  public void printNodeIDs() {
    for(Long id : nodeIds_) {
      System.out.println(" - "+id);
    }
  }
}
