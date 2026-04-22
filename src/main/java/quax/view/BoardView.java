package quax.view;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import quax.model.Board;
import quax.model.CellType;
import quax.model.GameState;
import quax.model.PlayerColor;
import quax.model.StrategyOverlay;

public class BoardView {

    private static final int BOARD_SIZE = 11;
    private static final int RHOMB_GRID_SIZE = 10;

    /**
     * Half of a horizontal/vertical octagon edge.
     */
    private static final double OCT_EDGE_HALF = 9;

    /**
     * Distance from octagon center to a horizontal/vertical edge.
     * Chosen to satisfy regular-octagon geometry for edge-contact tiling.
     */
    private static final double OCT_APOTHEM = OCT_EDGE_HALF * (1 + Math.sqrt(2));

    /**
     * Octagon center spacing needed for adjacent octagons to touch exactly.
     */
    private static final double GRID_STEP = OCT_APOTHEM * 2;

    /**
     * Rhombic (diamond) half-diagonal that exactly fills diagonal octagon gaps.
     */
    private static final double RHOMB_RADIUS = OCT_APOTHEM - OCT_EDGE_HALF;

    private static final double SIDE_BAND = 18;
    private static final double LABEL_MARGIN = 24;
    private static final double OUTER_PADDING = 14;

    private static final Color OCT_EMPTY = Color.web("#f28c00");
    private static final Color RHOMB_EMPTY = Color.web("#f2a33a");
    private static final Color GRID_STROKE = Color.web("#628cb3");
    private static final Color BLACK_SIDE = Color.BLACK;
    private static final Color WHITE_SIDE = Color.WHITE;
    private static final Color HEAT_LOW = Color.web("#2b6cb0");
    private static final Color HEAT_HIGH = Color.web("#f97316");
    private static final Color PATH_STROKE = Color.web("#fff066");

    private final Pane root = new Pane();
    private final Group boardLayer = new Group();
    private final Group moveOrderLayer = new Group();
    private final Group overlayLayer = new Group();

    private final Polygon[][] octCells = new Polygon[BOARD_SIZE][BOARD_SIZE];
    private final Polygon[][] rhombCells = new Polygon[RHOMB_GRID_SIZE][RHOMB_GRID_SIZE];
    private boolean showMoveOrder;

    public BoardView() {
        moveOrderLayer.setMouseTransparent(true);
        overlayLayer.setMouseTransparent(true);
        drawBoard();
    }

    public void drawBoard() {

        boardLayer.getChildren().clear();
        moveOrderLayer.getChildren().clear();
        overlayLayer.getChildren().clear();

        double boardLeft = OUTER_PADDING + LABEL_MARGIN + SIDE_BAND;
        double boardTop = OUTER_PADDING + LABEL_MARGIN + SIDE_BAND;
        double boardWidth = BOARD_SIZE * GRID_STEP;
        double boardHeight = BOARD_SIZE * GRID_STEP;

        drawGoalSides(boardLeft, boardTop, boardWidth, boardHeight);
        drawOctGrid(boardLeft, boardTop);
        drawRhombGrid(boardLeft, boardTop);
        drawCoordinateLabels(boardLeft, boardTop, boardWidth, boardHeight);

        root.getChildren().setAll(boardLayer, moveOrderLayer, overlayLayer);

        double prefWidth = boardLeft + boardWidth + SIDE_BAND + LABEL_MARGIN + OUTER_PADDING;
        double prefHeight = boardTop + boardHeight + SIDE_BAND + LABEL_MARGIN + OUTER_PADDING;

        root.setPrefSize(prefWidth, prefHeight);
    }

    private void drawGoalSides(double boardLeft, double boardTop, double boardWidth, double boardHeight) {

        double boardRight = boardLeft + boardWidth;
        double boardBottom = boardTop + boardHeight;

        // White goal sides (left/right), slightly extending into board cut-ins.
        Rectangle leftSide = new Rectangle(
                boardLeft - SIDE_BAND,
                boardTop,
                SIDE_BAND + RHOMB_RADIUS,
                boardHeight
        );
        leftSide.setFill(WHITE_SIDE);

        Rectangle rightSide = new Rectangle(
                boardRight - RHOMB_RADIUS,
                boardTop,
                SIDE_BAND + RHOMB_RADIUS,
                boardHeight
        );
        rightSide.setFill(WHITE_SIDE);

        // Black goal sides (top/bottom), slightly extending into board cut-ins.
        Rectangle topSide = new Rectangle(
                boardLeft,
                boardTop - SIDE_BAND,
                boardWidth,
                SIDE_BAND + RHOMB_RADIUS
        );
        topSide.setFill(BLACK_SIDE);

        Rectangle bottomSide = new Rectangle(
                boardLeft,
                boardBottom - RHOMB_RADIUS,
                boardWidth,
                SIDE_BAND + RHOMB_RADIUS
        );
        bottomSide.setFill(BLACK_SIDE);

        boardLayer.getChildren().addAll(leftSide, rightSide, topSide, bottomSide);
    }

    private void drawOctGrid(double boardLeft, double boardTop) {

        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {

                double centerX = boardLeft + OCT_APOTHEM + c * GRID_STEP;
                double centerY = boardTop + OCT_APOTHEM + r * GRID_STEP;

                Polygon oct = createOctagon(centerX, centerY);
                oct.setFill(OCT_EMPTY);
                oct.setStroke(GRID_STROKE);
                oct.setStrokeWidth(1.1);

                octCells[r][c] = oct;
                boardLayer.getChildren().add(oct);
            }
        }
    }

    private void drawRhombGrid(double boardLeft, double boardTop) {

        for (int r = 0; r < RHOMB_GRID_SIZE; r++) {
            for (int c = 0; c < RHOMB_GRID_SIZE; c++) {

                double centerX = boardLeft + OCT_APOTHEM + (c + 0.5) * GRID_STEP;
                double centerY = boardTop + OCT_APOTHEM + (r + 0.5) * GRID_STEP;

                Polygon rhomb = new Polygon(
                        centerX, centerY - RHOMB_RADIUS,
                        centerX + RHOMB_RADIUS, centerY,
                        centerX, centerY + RHOMB_RADIUS,
                        centerX - RHOMB_RADIUS, centerY
                );

                rhomb.setFill(RHOMB_EMPTY);
                rhomb.setStroke(GRID_STROKE);
                rhomb.setStrokeWidth(1.1);

                rhombCells[r][c] = rhomb;
                boardLayer.getChildren().add(rhomb);
            }
        }
    }

    private void drawCoordinateLabels(double boardLeft, double boardTop, double boardWidth, double boardHeight) {

        double boardRight = boardLeft + boardWidth;

        for (int c = 0; c < BOARD_SIZE; c++) {
            String letter = String.valueOf((char) ('A' + c));
            double centerX = boardLeft + OCT_APOTHEM + c * GRID_STEP;

            addCenteredLabel(letter, centerX, boardTop - SIDE_BAND - 8);
            addCenteredLabel(letter, centerX, boardTop + boardHeight + SIDE_BAND + 16);
        }

        for (int r = 0; r < BOARD_SIZE; r++) {
            int rowNumber = BOARD_SIZE - r;
            String rowText = String.valueOf(rowNumber);
            double centerY = boardTop + OCT_APOTHEM + r * GRID_STEP;

            addCenteredLabel(rowText, boardLeft - SIDE_BAND - 16, centerY);
            addCenteredLabel(rowText, boardRight + SIDE_BAND + 16, centerY);
        }
    }

    private void addCenteredLabel(String value, double centerX, double centerY) {

        Text text = new Text(value);
        text.setFill(Color.BLACK);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        double width = text.getLayoutBounds().getWidth();
        double height = text.getLayoutBounds().getHeight();
        text.setX(centerX - width / 2.0);
        text.setY(centerY + height / 4.0);

        boardLayer.getChildren().add(text);
    }

    private Polygon createOctagon(double centerX, double centerY) {

        Polygon polygon = new Polygon();

        polygon.getPoints().addAll(
                centerX - OCT_EDGE_HALF, centerY - OCT_APOTHEM,
                centerX + OCT_EDGE_HALF, centerY - OCT_APOTHEM,
                centerX + OCT_APOTHEM, centerY - OCT_EDGE_HALF,
                centerX + OCT_APOTHEM, centerY + OCT_EDGE_HALF,
                centerX + OCT_EDGE_HALF, centerY + OCT_APOTHEM,
                centerX - OCT_EDGE_HALF, centerY + OCT_APOTHEM,
                centerX - OCT_APOTHEM, centerY + OCT_EDGE_HALF,
                centerX - OCT_APOTHEM, centerY - OCT_EDGE_HALF
        );

        return polygon;
    }

    public void updateFrom(GameState state) {

        if (state == null) {
            return;
        }

        Board board = state.getBoard();

        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                PlayerColor occupant = board.getOct(r, c).getOccupant();
                octCells[r][c].setFill(colorForOccupant(occupant, OCT_EMPTY));
            }
        }

        for (int r = 0; r < RHOMB_GRID_SIZE; r++) {
            for (int c = 0; c < RHOMB_GRID_SIZE; c++) {
                PlayerColor occupant = board.getRhomb(r, c).getOccupant();
                rhombCells[r][c].setFill(colorForOccupant(occupant, RHOMB_EMPTY));
            }
        }

        drawMoveOrderOverlay(state);
    }

    public void setShowMoveOrder(boolean showMoveOrder, GameState state) {
        this.showMoveOrder = showMoveOrder;
        drawMoveOrderOverlay(state);
    }

    public boolean isShowingMoveOrder() {
        return showMoveOrder;
    }

    private void drawMoveOrderOverlay(GameState state) {
        moveOrderLayer.getChildren().clear();

        if (!showMoveOrder || state == null) {
            return;
        }

        Board board = state.getBoard();
        double boardLeft = OUTER_PADDING + LABEL_MARGIN + SIDE_BAND;
        double boardTop = OUTER_PADDING + LABEL_MARGIN + SIDE_BAND;

        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                int order = board.getOct(r, c).getMoveOrder();
                if (order > 0) {
                    double centerX = boardLeft + OCT_APOTHEM + c * GRID_STEP;
                    double centerY = boardTop + OCT_APOTHEM + r * GRID_STEP;
                    addMoveOrderLabel(order, centerX, centerY, board.getOct(r, c).getOccupant(), 15);
                }
            }
        }

        for (int r = 0; r < RHOMB_GRID_SIZE; r++) {
            for (int c = 0; c < RHOMB_GRID_SIZE; c++) {
                int order = board.getRhomb(r, c).getMoveOrder();
                if (order > 0) {
                    double centerX = boardLeft + OCT_APOTHEM + (c + 0.5) * GRID_STEP;
                    double centerY = boardTop + OCT_APOTHEM + (r + 0.5) * GRID_STEP;
                    addMoveOrderLabel(order, centerX, centerY, board.getRhomb(r, c).getOccupant(), 12);
                }
            }
        }
    }

    private void addMoveOrderLabel(
            int order,
            double centerX,
            double centerY,
            PlayerColor occupant,
            int fontSize
    ) {
        Text text = new Text(String.valueOf(order));
        text.getStyleClass().add("move-order-label");
        text.setMouseTransparent(true);
        text.setFill(occupant == PlayerColor.BLACK ? Color.WHITE : Color.BLACK);
        text.setStroke(occupant == PlayerColor.BLACK ? Color.BLACK : Color.WHITE);
        text.setStrokeWidth(0.45);
        text.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, fontSize));

        double width = text.getLayoutBounds().getWidth();
        double height = text.getLayoutBounds().getHeight();
        text.setX(centerX - width / 2.0);
        text.setY(centerY + height / 4.0);

        moveOrderLayer.getChildren().add(text);
    }

    public void drawStrategyOverlay(StrategyOverlay overlay) {

        overlayLayer.getChildren().clear();

        if (overlay == null) {
            return;
        }

        drawHeatmap(overlay.getWeights());
        drawSuggestedPath(overlay.getSuggestedPath());

        double y = 20;
        List<String> notes = overlay.getNotes();
        if (notes == null) {
            return;
        }

        for (String note : notes) {
            Text text = new Text(10, y, note);
            text.setFill(Color.DARKBLUE);
            text.getStyleClass().add("strategy-note");
            overlayLayer.getChildren().add(text);
            y += 18;
        }
    }

    public void clearStrategyOverlay() {
        overlayLayer.getChildren().clear();
    }

    private Color colorForOccupant(PlayerColor occupant, Color emptyColor) {

        if (occupant == null) {
            return emptyColor;
        }

        if (occupant == PlayerColor.BLACK) {
            return Color.BLACK;
        }

        return Color.WHITE;
    }

    public void setOnOctClicked(BiConsumer<Integer, Integer> onClick) {
        setOnOctClicked(onClick, null);
    }

    public void setOnOctClicked(
            BiConsumer<Integer, Integer> onLeftClick,
            BiConsumer<Integer, Integer> onRightClick
    ) {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                final int rr = r;
                final int cc = c;
                octCells[rr][cc].setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        if (onRightClick != null) {
                            onRightClick.accept(rr, cc);
                        }
                        return;
                    }

                    if (event.getButton() == MouseButton.PRIMARY && onLeftClick != null) {
                        onLeftClick.accept(rr, cc);
                    }
                });
            }
        }
    }

    public void setOnRhombClicked(BiConsumer<Integer, Integer> onClick) {
        setOnRhombClicked(onClick, null);
    }

    public void setOnRhombClicked(
            BiConsumer<Integer, Integer> onLeftClick,
            BiConsumer<Integer, Integer> onRightClick
    ) {
        for (int r = 0; r < RHOMB_GRID_SIZE; r++) {
            for (int c = 0; c < RHOMB_GRID_SIZE; c++) {
                final int rr = r;
                final int cc = c;
                rhombCells[rr][cc].setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        if (onRightClick != null) {
                            onRightClick.accept(rr, cc);
                        }
                        return;
                    }

                    if (event.getButton() == MouseButton.PRIMARY && onLeftClick != null) {
                        onLeftClick.accept(rr, cc);
                    }
                });
            }
        }
    }

    public Pane getRoot() {
        return root;
    }

    private void drawHeatmap(Map<String, Double> weights) {
        if (weights == null || weights.isEmpty()) {
            return;
        }

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (double value : weights.values()) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            OverlayCell cell = parseCellId(entry.getKey());
            if (cell == null) {
                continue;
            }

            Polygon source = polygonFor(cell);
            if (source == null) {
                continue;
            }

            double normalized = max == min ? 1.0 : (entry.getValue() - min) / (max - min);
            Color base = HEAT_LOW.interpolate(HEAT_HIGH, normalized);

            Polygon overlay = copyPolygon(source);
            overlay.setFill(new Color(base.getRed(), base.getGreen(), base.getBlue(), 0.18 + (0.30 * normalized)));
            overlay.setStroke(new Color(base.getRed(), base.getGreen(), base.getBlue(), 0.65));
            overlay.setStrokeWidth(1.2);
            overlay.getStyleClass().add("strategy-heat");
            overlayLayer.getChildren().add(overlay);
        }
    }

    private void drawSuggestedPath(List<String> suggestedPath) {
        if (suggestedPath == null || suggestedPath.isEmpty()) {
            return;
        }

        for (String cellId : suggestedPath) {
            OverlayCell cell = parseCellId(cellId);
            if (cell == null) {
                continue;
            }

            Polygon source = polygonFor(cell);
            if (source == null) {
                continue;
            }

            Polygon overlay = copyPolygon(source);
            overlay.setFill(Color.TRANSPARENT);
            overlay.setStroke(PATH_STROKE);
            overlay.setStrokeWidth(cell.cellType == CellType.OCT ? 3.2 : 2.6);
            overlay.getStyleClass().add("strategy-path");
            overlayLayer.getChildren().add(overlay);
        }
    }

    private Polygon polygonFor(OverlayCell cell) {
        if (cell.cellType == CellType.OCT) {
            if (!isOctIndex(cell.row, cell.col)) {
                return null;
            }
            return octCells[cell.row][cell.col];
        }

        if (!isRhombIndex(cell.row, cell.col)) {
            return null;
        }
        return rhombCells[cell.row][cell.col];
    }

    private Polygon copyPolygon(Polygon source) {
        Polygon overlay = new Polygon();
        overlay.getPoints().addAll(source.getPoints());
        overlay.setMouseTransparent(true);
        return overlay;
    }

    private OverlayCell parseCellId(String cellId) {
        if (cellId == null || cellId.isBlank()) {
            return null;
        }

        String[] parts = cellId.split(":");
        if (parts.length != 3) {
            return null;
        }

        try {
            CellType cellType = CellType.valueOf(parts[0]);
            int row = Integer.parseInt(parts[1]);
            int col = Integer.parseInt(parts[2]);
            return new OverlayCell(cellType, row, col);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean isOctIndex(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private boolean isRhombIndex(int row, int col) {
        return row >= 0 && row < RHOMB_GRID_SIZE && col >= 0 && col < RHOMB_GRID_SIZE;
    }

    private static final class OverlayCell {
        private final CellType cellType;
        private final int row;
        private final int col;

        private OverlayCell(CellType cellType, int row, int col) {
            this.cellType = cellType;
            this.row = row;
            this.col = col;
        }
    }
}
