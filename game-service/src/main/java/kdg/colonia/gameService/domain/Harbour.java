package kdg.colonia.gameService.domain;

import kdg.colonia.gameService.domain.coordinates.Coordinate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Harbour {
    private Coordinate coordinate;
    //in case of 4:1; ratio = 4
    private int ratio;
    //use NOTHING in case of generic harbour
    private Resource resource;

    /**
     * Copying an existing object into a new object but referencing a new object
     *
     * @param harbour the harbour to copy
     */
    public Harbour(Harbour harbour) {
        this.coordinate = harbour.getCoordinate();
        this.ratio = harbour.getRatio();
        this.resource = harbour.getResource();
    }

    @Override
    public String toString() {
        return resource.toString() + " - "+ratio+":1 - ["+coordinate.getX()+","+coordinate.getY()+","+coordinate.getCardDir().toString()+"]";
    }

    public List<Coordinate> getAccessCoordinates(){
        return this.coordinate.calculateConnectedVerticesForEdge();
    }
}
