import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CBattleShip {
    public static final String MISSED = "missed";
    public static final String HIT = "hit";
    static int gridSize;
    CPlayer P1;
    CPlayer P2;
    int totalShipsToInsert;
    int totalShipsToSink;
    int numShipLittle;
    int numShipMedium;
    int numShipLarge;

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
        P1 = new CPlayer();
        P2 = new CPlayer();
        gridSize = 6;
        totalShipsToInsert = 3;
        totalShipsToSink = 2;
        numShipLarge = 1;
        numShipMedium = 1;
        numShipLittle = 1;
    }
    public void startGame(){

    }
    public String generateRules()
    {
        StringBuilder sbRules = new StringBuilder();
        //TODO
        return sbRules.toString();
    }
    private void displayGrid(CPlayer player){
        StringBuilder sb = new StringBuilder();
        for(int r = 0; r <player.shipGrid.length; r++)
        {
            for(int c = 0; c < player.shipGrid[r].length;c++)
            {
                String gridCell = player.shipGrid[r][c];
                String symbol;
//                switch(gridCell)
//                {
//                    case "little":
//                    case "medium":
//                    case "large":
//                        symbol = "O";
//                        break;
//                    case "":
//                        symbol = "";
//                    default:
//                        throw new IllegalStateException("Unexpected value: " + gridCell);
//                }
                if  (   gridCell.equals(String.valueOf(ShipType.little)) ||
                        gridCell.equals(String.valueOf(ShipType.medium))||
                        gridCell.equals(String.valueOf(ShipType.large))   )
                    symbol = "O";
                else if ( gridCell.equals(MISSED))
                    symbol = "/";
                else if (gridCell.equals(HIT))
                    symbol = "X";
                else if (gridCell.isEmpty())
                    symbol = "";
                else
                    throw new IllegalStateException("Unexpected value: " + gridCell);
                sb.append("| " + "  \n");
                sb.append("| " + " " + symbol + "\n");
                sb.append("| " + "  \n");
                sb.append("___" + "  \n");
                //TODO
            }
        }
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
        for(Pair<Integer,Integer> cord : coordList)
        {
            currPlayer.shipGrid[cord.getKey()][cord.getValue()] = String.valueOf(shipType);  //key is the row number,value is column number
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
                        return ShotOutcome.hit;
                }
            }
            return ShotOutcome.hitAndSunk;
        }
    }

    public boolean CheckWinningCondition(CPlayer playerToCheck)
    {
        for(int r = 0; r < playerToCheck.shipGrid.length; r++){
            for(int c = 0; c < playerToCheck.shipGrid[r].length; c++){
                if(!(playerToCheck.shipGrid[r][c].equals(MISSED)
                    ||playerToCheck.shipGrid[r][c].equals(HIT)
                    ||playerToCheck.shipGrid[r][c].isEmpty()))
                {
                    return false;
                }
            }
        }
        return  true;
    }

    public boolean validateCoordinates(CPlayer player, List<Pair<Integer,Integer>> coordList,int numExpectedCoord){
        if(coordList.isEmpty())return false;
        for (int i = 0; i < coordList.size(); i++) {
            if(i >= numExpectedCoord)
                return false;
            Pair<Integer, Integer> coord = coordList.get(i);
            int row = coord.getKey();
            int col = coord.getValue();
            if (row >= gridSize || col >= gridSize)
                return false;
            if (row < 0 || col < 0)
                return false;
            if (player.shipGrid[row][col].equals(HIT) || player.shipGrid[row][col].equals(MISSED))
                return false;
        }
        return true;
    }

    public static class CPlayer{
        String[][] shipGrid;
        int ShipsWreckedCount;


        public CPlayer()
        {
            ShipsWreckedCount = 0;
            shipGrid = new String[gridSize][gridSize];
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
