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

/**
 *
 * @author demory
 */
public class OSMNode {

  private long id_;
  private double lon_, lat_;
  
  public OSMNode(long id, double lon, double lat) {
    id_ = id;
    lon_ = lon;
    lat_ = lat;
  }
  
  public long getID() {
    return id_;
  }
  
  public double getLat() {
    return lat_;
  }
  
  public double getLon() {
    return lon_;
  }
}
