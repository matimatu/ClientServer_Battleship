import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CBattleShip {
    static final String MISSED = "missed";
    static final String HIT = "hit";
    static final int NUM_SINKINGS_TO_LOSE = 2;
    static int gridSize;
//    CPlayer P1;
//    CPlayer P2;
    int totalShipsToInsert;
    int totalShipsToSink;
    int numShipLittle;
    int numShipMedium;
    int numShipLarge;
    boolean ended;

    enum ShipType {
        little,     //1 piece
        medium,     //2 pieces
        large,      //3 pieces
    }
    enum ShotOutcome {
        hit,
        hitAndSunk,
        missed,
        invalidCoordinates,
    }
    public CBattleShip()
    {
        gridSize = 6;
//        P1 = new CPlayer();
//        P2 = new CPlayer();
        totalShipsToInsert = 3;
        totalShipsToSink = 2;
        numShipLarge = 1;
        numShipMedium = 1;
        numShipLittle = 1;
        ended = false;
    }
    public void startGame(){

    }
    public String generateRules()
    {
        StringBuilder sbRules = new StringBuilder();
        //TODO
        return sbRules.toString();
    }
    public String displayGrid(CPlayer player,boolean isEnemy){
        StringBuilder sb = new StringBuilder();
        char letter = 'A';
        for (int i = 0; i < gridSize; i++)
        {
            sb.append("   " + (letter++) + " ");
        }
        sb.append("\n");
        for(int r = 0; r <player.shipGrid.length; r++)
        {
            sb.append(r + "|");
            for(int c = 0; c < player.shipGrid[r].length;c++)
            {
                String gridCell = player.shipGrid[r][c];
                String symbol = getSymbol(gridCell,isEnemy);
                sb.append("_" + symbol + "_|" );
            }
            sb.append("\n" );
        }
        return sb.toString();
    }

    private String getSymbol(String gridCell,boolean isEnemy) {
        String symbol;
        if  (gridCell.equals(String.valueOf(ShipType.little)))
            symbol = isEnemy ? " " : "1";
        else if(gridCell.equals(String.valueOf(ShipType.medium)))
            symbol = isEnemy ? " " : "2";
        else if (gridCell.equals(String.valueOf(ShipType.large)))
            symbol = isEnemy ? " " : "3";
        else if ( gridCell.equals(MISSED))
            symbol = "/";
        else if (gridCell.equals(HIT))
            symbol = "X";
        else if (gridCell.isEmpty())
            symbol = " ";
        else
            throw new IllegalStateException("Unexpected value: " + gridCell);
        return symbol;
    }

    private List<Pair<Integer,Integer>>  extractCoordinates(String input)
    {
        List<Pair<Integer,Integer>> coordinates = new ArrayList<Pair<Integer,Integer>>();
        try
        {
            String[] arrayString = input.trim().toUpperCase().split(",");
            for(String string : arrayString)
            {
                char cRow = string.charAt(1);
                int row = Integer.parseInt(String.valueOf(cRow));
                char cColumn = string.charAt(0);
                int column = switch (cColumn) {
                    //TODO: semplificare con trick per ascii
                    case 'A' -> 0;
                    case 'B' -> 1;
                    case 'C' -> 2;
                    case 'D' -> 3;
                    case 'E' -> 4;
                    case 'F' -> 5;
                    default -> throw new IllegalStateException(cColumn + " unmanaged!");
                };
                coordinates.add(new Pair<>(row,column));
            }
        }
       catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("ERROR on extracting coordinates from client input");
       }
        return coordinates;
    }

//    public void insertShip(CPlayer player, ShipType shipType,String rawCoord )
//    {
//        List<Pair<Integer,Integer>> coordList = extractCoordinates(rawCoord);
//        for(Pair<Integer,Integer> cord : coordList)
//        {
//            player.shipGrid[cord.getKey()][cord.getValue()] = String.valueOf(shipType);  //key is the row number,value is column number
//        }
//    }
    public boolean insertShip(CPlayer currPlayer, ShipType shipType, String rawCoord )
    {
        List<Pair<Integer,Integer>> coordList = extractCoordinates(rawCoord);
        int numExpectedCoord = -1;
        switch(shipType)
        {
            case little -> numExpectedCoord = 1;
            case medium -> numExpectedCoord = 2;
            case large ->  numExpectedCoord = 3;
            default -> throw new IllegalStateException("Unexpected value: " + shipType);
        }
        if(!validateCoordinates(currPlayer,coordList,numExpectedCoord)) return false;
        for(Pair<Integer,Integer> coord : coordList)
        {
            int row = coord.getKey();
            int col = coord.getValue();
            if(!currPlayer.shipGrid[row][col].isEmpty())
                return  false;           //the player inserted a coordinate already filled
            currPlayer.shipGrid[row][col] = String.valueOf(shipType);  //key is the row number,value is column number
        }
        return  true;
    }

    public ShotOutcome fire(CPlayer playerToHit, String rawCoord )
    {
        List<Pair<Integer,Integer>> coordList = extractCoordinates(rawCoord);
        if(!validateCoordinates(playerToHit,coordList,1))
            return ShotOutcome.invalidCoordinates;
        Pair<Integer,Integer> coord = coordList.get(0);
        int row = coord.getKey();
        int column = coord.getValue();
        if(playerToHit.shipGrid[row][column].isEmpty())
        {
            playerToHit.shipGrid[row][column] = MISSED;
            return ShotOutcome.missed;
        }
//        else if(playerToHit.shipGrid[coord.getKey()][coord.getValue()].equals(String.valueOf(ShipType.little))
//        || playerToHit.shipGrid[coord.getKey()][coord.getValue()].equals(String.valueOf(ShipType.medium))
//        || playerToHit.shipGrid[coord.getKey()][coord.getValue()].equals(String.valueOf(ShipType.large)))
        else
        {
            String shipHitName = playerToHit.shipGrid[row][column];
            playerToHit.shipGrid[row][column] = HIT;
            for(int r = 0; r < playerToHit.shipGrid.length;r++)
            {
                for(int c = 0; c < playerToHit.shipGrid[r].length;c++)
                {
                    if(playerToHit.shipGrid[r][c].equals(shipHitName))
                    {
                        return ShotOutcome.hit;
                    }
                }
            }
            playerToHit.sunkenShipsCount++;
            return ShotOutcome.hitAndSunk;
        }
    }

    public boolean CheckLosingCondition(CPlayer playerToCheck)
    {
//        for(int r = 0; r < playerToCheck.shipGrid.length; r++){
//            for(int c = 0; c < playerToCheck.shipGrid[r].length; c++){
//                if(!(playerToCheck.shipGrid[r][c].equals(MISSED)
//                    ||playerToCheck.shipGrid[r][c].equals(HIT)
//                    ||playerToCheck.shipGrid[r][c].isEmpty()))
//                {
//                    return false;
//                }
//            }
//        }
//        return  true;
        return playerToCheck.sunkenShipsCount == NUM_SINKINGS_TO_LOSE;
    }

    public boolean validateCoordinates(CPlayer player, List<Pair<Integer,Integer>> coordList,int numExpectedCoord){
        if(coordList.size() != numExpectedCoord)
            return false;       //wrong number of coordinates expected
        List<Pair<Integer,Integer>> coordNearList = new ArrayList<Pair<Integer,Integer>>();
        for (int i= 0;i < coordList.size(); i++)
        {
            Pair<Integer, Integer> coord = coordList.get(i);
            int row = coord.getKey();
            int col = coord.getValue();
            if (row >= gridSize || col >= gridSize)
                return false;       //coordinate exceed grid size
            if (row < 0 || col < 0)
                return false;       //coordinate exceed grid size
            if (player.shipGrid[row][col].equals(HIT) || player.shipGrid[row][col].equals(MISSED))  //control only for shots
                return false;       //the shot already goes into that coordinate
            coordNearList.add(coord);

            for(int x = 0; x < gridSize; x++)
            {

            }
        }
        boolean nearUp = false;
        boolean nearDown = false;
        boolean nearLeft = false;
        boolean nearRight = false;

        for (int i= 0;i < coordList.size(); i++)
        {
            Pair<Integer, Integer> coord = coordList.get(i);
            int row = coord.getKey();
            int col = coord.getValue();
            boolean coordNear = false;
            for(int j=i+1;j<coordList.size();j++)
            {
                int rowCurr = coordList.get(j).getKey();
                int colCurr = coordList.get(j).getValue();
                boolean repeatedCoord = row == rowCurr && col == colCurr;
                if (repeatedCoord)
                    return false;

            }
            //control on cells to be near each other
            boolean nearCondition = false;
            if(coordList.size() > 1)
            {
                for(int j=0;j<coordList.size();j++)
                {
                    int rowCurr = coordList.get(j).getKey();
                    int colCurr = coordList.get(j).getValue();
                    nearUp = rowCurr == row -1 && colCurr == col;
                    nearDown =  rowCurr == row +1 && colCurr == col;
                    nearLeft = rowCurr == row && colCurr == col-1;
                    nearRight = rowCurr == row && colCurr == col+1;
                    if(!nearCondition)
                    {
                        if(row == 0)
                        {
                            if(col == 0)
                            {
                                nearCondition = ( nearRight || nearDown );
                            }
                            else if(col ==gridSize-1)
                            {
                                nearCondition = ( nearLeft || nearDown);
                            }
                            else
                            {
                                nearCondition = (   nearLeft || nearUp || nearDown);
                            }
                        }
                        else if (col == 0)
                        {
                            if(row ==gridSize-1)
                            {
                                nearCondition = ( nearUp || nearRight);
                            }
                            else
                            {
                                nearCondition = (   nearRight || nearUp || nearDown);
                            }
                        }
                        else if(row == gridSize-1 && col == gridSize-1)
                        {
                            nearCondition = ( nearUp || nearLeft);
                        }
                        else {
                            nearCondition = (nearLeft || nearRight || nearUp || nearDown);
                        }
                    }
                    coordNear = nearCondition;
                }
                if(!coordNear)
                    return false;
            }
        }
        return true;
    }

    public static class CPlayer{
        String[][] shipGrid;
        int sunkenShipsCount;       // number of sunken ships of the player
        String name;

        public CPlayer()
        {
            sunkenShipsCount = 0;
            shipGrid = new String[gridSize][gridSize];
            name = "";
            for (String[] strings : shipGrid) {
                Arrays.fill(strings, "");
            }
            //            for (int r = 0; r < shipGrid.length; r++) {
            //                for (int c = 0; c < shipGrid[r].length; c++) {
            //                    shipGrid[r][c] = "";
            //                }
            //            }
        }
    }
}
