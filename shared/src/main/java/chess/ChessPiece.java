package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        List<ChessMove> MoveList = new ArrayList<>();
        List<ChessMove> BlockedList = new ArrayList<>();

        if (piece.getPieceType() == PieceType.BISHOP){
            int[] offsets = {1, 2, 3, 4, 5, 6, 7};

            for(int dx : offsets){
                //Future Position
                int mx = myPosition.getColumn() + dx;
                int my = myPosition.getRow() + dx;

                //Dont go off board
                if(my < 1 | my > 8){continue;}
                if(mx < 1 | mx > 8){continue;}


                //Check to see if enemy piece is there
                if(board.getPiece(new ChessPosition(my,mx)) != null) {
                    if (board.getPiece(new ChessPosition(my,mx)).getTeamColor() == piece.getTeamColor()){continue;}
                }
                MoveList.add(new ChessMove(myPosition, new ChessPosition(my,mx), null));
            }
            for(int dx : offsets){
                //Future Position
                int mx = myPosition.getColumn() + dx;
                int my = myPosition.getRow() - dx;

                //Dont go off board
                if(my < 1 | my > 8){continue;}
                if(mx < 1 | mx > 8){continue;}


                //Check to see if enemy piece is there
                if(board.getPiece(new ChessPosition(my,mx)) != null) {
                    if (board.getPiece(new ChessPosition(my,mx)).getTeamColor() == piece.getTeamColor()){continue;}
                }
                MoveList.add(new ChessMove(myPosition, new ChessPosition(my,mx), null));
            }
            for(int dx : offsets){
                //Future Position
                int mx = myPosition.getColumn() - dx;
                int my = myPosition.getRow() + dx;

                //Dont go off board
                if(my < 1 | my > 8){continue;}
                if(mx < 1 | mx > 8){continue;}


                //Check to see if enemy piece is there
                if(board.getPiece(new ChessPosition(my,mx)) != null) {
                    if (board.getPiece(new ChessPosition(my,mx)).getTeamColor() == piece.getTeamColor()){continue;}
                }
                MoveList.add(new ChessMove(myPosition, new ChessPosition(my,mx), null));
            }
            for(int dx : offsets){
                //Future Position
                int mx = myPosition.getColumn() - dx;
                int my = myPosition.getRow() - dx;

                //Dont go off board
                if(my < 1 | my > 8){continue;}
                if(mx < 1 | mx > 8){continue;}


                //Check to see if enemy piece is there
                if(board.getPiece(new ChessPosition(my,mx)) != null) {
                    if (board.getPiece(new ChessPosition(my,mx)).getTeamColor() == piece.getTeamColor()){continue;}
                }
                MoveList.add(new ChessMove(myPosition, new ChessPosition(my,mx), null));
            }

        }

        if (piece.getPieceType() == PieceType.KING){
            int[] offsets = {1,0,-1};

            for(int dx : offsets){
                for(int dy : offsets){
                    //Future Position
                    int mx = myPosition.getColumn() + dx;
                    int my = myPosition.getRow() + dy;

                    //Make sure the king isnt staying still
                    if(dx == 0 && dy == 0){continue;}

                    //Dont go off board
                    if(mx > 7 | my > 7){continue;}

                    //Check to see if enemy piece is there
                    if(board.getPiece(new ChessPosition(my,mx)) != null) {
                        if (board.getPiece(new ChessPosition(my,mx)).getTeamColor() == piece.getTeamColor()){continue;}
                    }

                    MoveList.add(new ChessMove(myPosition, new ChessPosition(my,mx), null));

                }
            }
        }

        if (piece.getPieceType() == PieceType.KNIGHT){
            return List.of(new ChessMove(new ChessPosition(5,4) , new ChessPosition(1,8),null));
        }

        if (piece.getPieceType() == PieceType.PAWN){

            return List.of(new ChessMove(new ChessPosition(5,4) , new ChessPosition(1,8),null));
        }

        if (piece.getPieceType() == PieceType.QUEEN){
            return List.of(new ChessMove(new ChessPosition(5,4) , new ChessPosition(1,8),null));
        }

        if (piece.getPieceType() == PieceType.ROOK){
            int[] offsets = {1, -1, -2, 2, 3,-3, 4, -4, -5, 5, -6, 6, -7, 7, -8, 8};

                for(int dx : offsets){
                    //Future Position
                    int mx = myPosition.getColumn() + dx;

                    //Dont go off board
                    if(mx > 8 |  mx < 1){continue;}

                    //Check to see if piece is there
                    if(board.getPiece(new ChessPosition(myPosition.getRow(), mx)) != null) {
                        if (board.getPiece(new ChessPosition(myPosition.getRow(),mx)).getTeamColor() == piece.getTeamColor()){
                            if(mx < myPosition.getColumn()){
                                for(int i = 0; i <= mx; i++){
                                    BlockedList.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow(), i), null));
                                }
                            }else{
                                for(int i = 8; i >= mx; i--){
                                    BlockedList.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow(), i), null));
                                }
                            }
                        }else{
                            if(mx < myPosition.getColumn()){
                                for(int i = 0; i < mx; i++){
                                    BlockedList.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow(), i), null));
                                }
                            }else{
                                for(int i = 8; i > mx; i--){
                                    BlockedList.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow(), i), null));
                                }
                            }
                        }
                    }

                    MoveList.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow(), mx), null));

                }
                for(int dy : offsets){
                    //Future Position
                    int my = myPosition.getRow() + dy;

                    //Dont go off board
                    if(my > 8|  my < 1){continue;}

                    //Check to see if enemy piece is there
                    if(board.getPiece(new ChessPosition(my, myPosition.getColumn())) != null) {
                        if (board.getPiece(new ChessPosition(my,myPosition.getColumn())).getTeamColor() == piece.getTeamColor()){
                            if(my < myPosition.getRow()){
                                for(int i = 0; i <= my; i++){
                                    BlockedList.add(new ChessMove(myPosition, new ChessPosition(i, myPosition.getColumn()), null));
                                }
                            }else{
                                for(int i = 8; i >= my; i--){
                                    BlockedList.add(new ChessMove(myPosition, new ChessPosition(i, myPosition.getColumn()), null));
                                }
                            }
                        }else{
                            if(my < myPosition.getRow()){
                                for(int i = 0; i < my; i++){
                                    BlockedList.add(new ChessMove(myPosition, new ChessPosition(i, myPosition.getColumn()), null));
                                }
                            }else{
                                for(int i = 8; i > my; i--){
                                    BlockedList.add(new ChessMove(myPosition, new ChessPosition(i, myPosition.getColumn()), null));
                                }
                            }
                        }
                    }

                    MoveList.add(new ChessMove(myPosition, new ChessPosition(my,myPosition.getColumn()), null));

                }
        }
        System.out.println(BlockedList);
        System.out.println("this is move list: " + MoveList);

        for(ChessMove i : BlockedList){
            MoveList.remove(i);
        }
        System.out.println(BlockedList);
        System.out.println("this is move list: " + MoveList);

        return MoveList;
    }
}