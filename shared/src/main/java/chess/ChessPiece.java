package chess;

import java.util.*;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
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
        ArrayList<ChessMove> MoveList = new ArrayList<>();
        if(type==PieceType.QUEEN){
            slideMoves(board,MoveList,myPosition,8,true,true);
        }
        if(type==PieceType.ROOK){
            slideMoves(board,MoveList,myPosition,8,false,true);
        }
        if(type==PieceType.BISHOP){
            slideMoves(board,MoveList,myPosition,8,true,false);
        }
        if(type==PieceType.KING){
            slideMoves(board,MoveList,myPosition,1,true,true);
        }
        if(type==PieceType.KNIGHT){
            slideMoves(board,MoveList,myPosition,1,false,false);
        }
        if(type==PieceType.PAWN){
            pawnMoves(board,MoveList,myPosition);
        }


        return MoveList;
    }
    public void slideMoves(ChessBoard board, ArrayList<ChessMove> moves, ChessPosition start, int mult, boolean diag, boolean straight){
        ArrayList<int[]> directions = new ArrayList<>();
        if(straight){
            directions.add(new int[]{0, 1});
            directions.add(new int[]{0, -1});
            directions.add(new int[]{1, 0});
            directions.add(new int[]{-1, 0});
        }
        if(diag){
            directions.add(new int[]{1,1});
            directions.add(new int[]{1,-1});
            directions.add(new int[]{-1,1});
            directions.add(new int[]{-1,-1});
        }
        if(!diag && !straight){
            directions.add(new int[]{1,2});
            directions.add(new int[]{1,-2});
            directions.add(new int[]{-1,2});
            directions.add(new int[]{-1,-2});
            directions.add(new int[]{2,1});
            directions.add(new int[]{2,-1});
            directions.add(new int[]{-2,1});
            directions.add(new int[]{-2,-1});
        }
        for(int[] dir:directions){
            for(int i = 1; i <= mult; i++){
                int mx = start.getColumn() + (dir[0] * i);
                int my = start.getRow() + (dir[1] * i);
                if(inBounds(my, mx)){break;}
                ChessPosition target = new ChessPosition(my,mx);
                ChessPiece victim = board.getPiece(target);
                if(victim != null){
                    if(victim.getTeamColor()!=pieceColor){
                        moves.add(new ChessMove(start,target,null));
                    }
                    break;

                }else{
                    moves.add(new ChessMove(start,target,null));
                }


            }

        }
    }

    public void pawnMoves(ChessBoard board, ArrayList<ChessMove> moves, ChessPosition start){
        int startRow = 2;
        int promoRow = 8;
        int direction = 1;
        if(pieceColor != ChessGame.TeamColor.WHITE){
            startRow = 7;
            promoRow = 1;
            direction = -1;
        }
        int mx = start.getColumn();
        int my = start.getRow();

        if(!inBounds(my + direction, mx)){
            ChessPosition targetOne = new ChessPosition(my+direction,mx);
            if(board.getPiece(targetOne) == null){
                pawnEndMove(start,targetOne,moves,promoRow);
                if(!inBounds(my+direction*2,mx) && my == startRow){
                    ChessPosition targetTwo = new ChessPosition(my+direction*2,mx);
                    if(board.getPiece(targetTwo) == null){
                        pawnEndMove(start,targetTwo,moves,promoRow);
                    }
                }
            }

        }
        if(!inBounds(my+direction,mx+1)){
            ChessPosition targetSide = new ChessPosition(my+direction,mx+1);
            if(board.getPiece(targetSide) != null && board.getPiece(targetSide).getTeamColor() != pieceColor ){
                pawnEndMove(start,targetSide,moves,promoRow);
            }
        }
        if(!inBounds(my+direction,mx-1)){
            ChessPosition targetSide = new ChessPosition(my+direction,mx-1);
            if(board.getPiece(targetSide) != null && board.getPiece(targetSide).getTeamColor() != pieceColor ){
                pawnEndMove(start,targetSide,moves,promoRow);
            }
        }

    }

    public static boolean inBounds(int row, int col){
        return row > 8 || row < 1 || col > 8 || col < 1;
    }

    public void pawnEndMove(ChessPosition start, ChessPosition end,ArrayList<ChessMove> moves, int promo){
        if(end.getRow() == promo){
            moves.add(new ChessMove(start,end,PieceType.QUEEN));
            moves.add(new ChessMove(start,end,PieceType.BISHOP));
            moves.add(new ChessMove(start,end,PieceType.ROOK));
            moves.add(new ChessMove(start,end,PieceType.KNIGHT));
        }else{
            moves.add(new ChessMove(start,end,null));
        }
    }
}
