# these are some notes

## Classes

### ChessGame
validMoves:
position -> all legal moves

makeMove:
move -> execution

isInCheck:
team -> bool

isInCheckmate:
team -> bool

isInStalemate:
team -> bool

### ChessBoard
stores all uncaptured pieces

must implement adding, removing, and resetBoard()

### ChessPiece
contains piecetype enum for types of pieces

pieceMoves:
like validMoves except doesn't care about turn or king being in check

### ChessMove

represents a possible move

!! implement toString to make it show chess notation
### ChessPosition
represents location



