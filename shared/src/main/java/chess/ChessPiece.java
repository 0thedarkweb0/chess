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

        if (piece.getPieceType() == PieceType.BISHOP){
            slideMoves(board, myPosition, MoveList, true, false);
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

            int[][] offsets = {{ 2,  1}, { 2, -1}, {-2,  1}, {-2, -1},
                    { 1,  2}, { 1, -2}, {-1,  2}, {-1, -2}};

            for(int[] dxdy : offsets){
                //Future Position
                int mx = myPosition.getColumn() + dxdy[1];
                int my = myPosition.getRow() + dxdy[0];

                //Dont go off board
                if ( my < 1 || mx < 1 || my > 8 || mx > 8 ){continue;}

                //Check to see if enemy piece is there
                if(board.getPiece(new ChessPosition(my,mx)) != null) {
                    if (board.getPiece(new ChessPosition(my,mx)).getTeamColor() == piece.getTeamColor()){continue;}
                }

                MoveList.add(new ChessMove(myPosition, new ChessPosition(my,mx), null));
            }
        }

        if (piece.getPieceType() == PieceType.PAWN){

            return List.of(new ChessMove(new ChessPosition(5,4) , new ChessPosition(1,8),null));
        }

        if (piece.getPieceType() == PieceType.QUEEN){
            slideMoves(board, myPosition, MoveList, true, true);
        }

        if (piece.getPieceType() == PieceType.ROOK){
            slideMoves(board, myPosition, MoveList, false, true);
        }

        return MoveList;
    }

    /**
     * Calculates the movement for pieces that have "sliding" motion.
     * */
    private void slideMoves(ChessBoard board, ChessPosition start, List<ChessMove> moves, boolean diag, boolean straight){
        List<int[]> directions = new ArrayList<>();
        if(straight){
            directions.add(new int[]{1,0});
            directions.add(new int[]{-1,0});
            directions.add(new int[]{0,1});
            directions.add(new int[]{0,-1});
        }
        if(diag){
            directions.add(new int[]{1,1});
            directions.add(new int[]{-1,1});
            directions.add(new int[]{1,-1});
            directions.add(new int[]{-1,-1});
        }
        for(int[] dir :directions){
            for(int mult = 1; mult <= 7; mult++){
                int my = start.getRow() + (mult * dir[0]);
                int mx = start.getColumn() + (mult * dir[1]);

                if ( my < 1 || mx < 1 || my > 8 || mx > 8 ){break;}

                ChessPosition targetPos = new ChessPosition(my,mx);
                ChessPiece targetPiece = board.getPiece(targetPos);

                if(targetPiece != null){
                    if (targetPiece.getTeamColor() != pieceColor){
                        moves.add(new ChessMove(start, targetPos, null));
                    }
                    break; //
                }
                moves.add(new ChessMove(start, targetPos, null));
            }

        }
    }
}