/*
 * Copyright 2006-2020 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.modules.visualization.ims.imsvisualizer;

import com.google.common.collect.Range;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.Scan;
import io.github.mzmine.gui.chartbasics.chartthemes.EStandardChartTheme;
import io.github.mzmine.gui.chartbasics.chartutils.XYBlockPixelSizePaintScales;
import io.github.mzmine.gui.chartbasics.chartutils.XYBlockPixelSizeRenderer;
import io.github.mzmine.gui.chartbasics.gui.javafx.EChartViewer;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.visualization.ims.ImsVisualizerTask;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYZDataset;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

public class MzMobilityHeatMapPlot extends EChartViewer {

    private XYPlot plot;
    private JFreeChart chart;
    private XYZDataset dataset3d;
    static final Font legendFont = new Font("SansSerif", Font.PLAIN, 12);
    private EStandardChartTheme theme;
    private final double selectedRetentionTime;
    private PaintScaleLegend legend;
    private XYBlockPixelSizeRenderer pixelRenderer;
    private XYBlockRenderer blockRenderer;

    public MzMobilityHeatMapPlot(
            XYZDataset dataset,
            String paintScaleStyle,
            ImsVisualizerTask imsTask,
            IntensityMobilityPlot implot) {

        super(
                ChartFactory.createScatterPlot(
                        "", "m/z", "mobility", dataset, PlotOrientation.VERTICAL, true, true, true));

        chart = getChart();
        this.dataset3d = dataset;
        this.selectedRetentionTime = imsTask.getSelectedRetentionTime();

        // copy and sort z-Values for min and max of the paint scale
        double[] copyZValues = new double[dataset3d.getItemCount(0)];
        for (int i = 0; i < dataset3d.getItemCount(0); i++) {
            copyZValues[i] = dataset3d.getZValue(0, i);
        }
        Arrays.sort(copyZValues);

        // copy and sort x-values.
        double[] copyXValues = new double[dataset3d.getItemCount(0)];
        for (int i = 0; i < dataset3d.getItemCount(0); i++) {
            copyXValues[i] = dataset3d.getXValue(0, i);
        }
        Arrays.sort(copyXValues);

        // copy and sort y-values.
        double[] copyYValues = new double[dataset3d.getItemCount(0)];
        for (int i = 0; i < dataset3d.getItemCount(0); i++) {
            copyYValues[i] = dataset3d.getYValue(0, i);
        }
        Arrays.sort(copyYValues);

        // get index in accordance to percentile windows
        int minIndexScale = 0;
        int maxIndexScale = copyZValues.length - 1;
        double min = copyZValues[minIndexScale];
        double max = copyZValues[maxIndexScale];
        Paint[] contourColors =
                XYBlockPixelSizePaintScales.getPaintColors(
                        "", Range.closed(min, max), paintScaleStyle);
        LookupPaintScale scale = new LookupPaintScale(min, max, Color.BLACK);

        double[] scaleValues = new double[contourColors.length];
        double delta = (max - min) / (contourColors.length - 1);
        double value = min;
        for (int i = 0; i < contourColors.length; i++) {
            scaleValues[i] = value;
            scale.add(value, contourColors[i]);
            value = value + delta;
        }
        plot = chart.getXYPlot();
        theme = MZmineCore.getConfiguration().getDefaultChartTheme();
        theme.apply(chart);

        // set the pixel renderer
        setPixelRenderer(copyXValues, copyYValues, scale);
        // Legend
        prepareLegend(min, max, scale);

        // Set paint scale
        blockRenderer.setPaintScale(scale);

        plot.setRenderer(blockRenderer);
        plot.setBackgroundPaint(Color.black);
        plot.setRangeGridlinePaint(Color.black);
        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
        plot.setOutlinePaint(Color.black);
        chart.addSubtitle(legend);

        // mouse listener.
        addChartMouseListener(
                new ChartMouseListenerFX() {
                    @Override
                    public void chartMouseClicked(ChartMouseEventFX event) {
                        ChartEntity chartEntity = event.getEntity();

                        if (event.getTrigger().getClickCount() == 2) {
                            // if chartintity is not selected any valid point find it's nearest.
                            if (!(chartEntity instanceof XYItemEntity)) {
                                int x = (int) ((event.getTrigger().getX() - getInsets().getLeft()) / getScaleX());
                                int y = (int) ((event.getTrigger().getY() - getInsets().getRight()) / getScaleY());
                                Point2D point2d = new Point2D.Double(x, y);
                                double minDistance = Integer.MAX_VALUE;
                                Collection entities = getRenderingInfo().getEntityCollection().getEntities();
                                for (Iterator iter = entities.iterator(); iter.hasNext(); ) {
                                    ChartEntity element = (ChartEntity) iter.next();

                                    if (element instanceof XYItemEntity) {
                                        Rectangle rect = element.getArea().getBounds();
                                        Point2D centerPoint = new Point2D.Double(rect.getCenterX(), rect.getCenterY());

                                        if (point2d.distance(centerPoint) < minDistance) {
                                            minDistance = point2d.distance(centerPoint);
                                            chartEntity = element;
                                        }
                                    }
                                }
                            }

                            if (chartEntity instanceof XYItemEntity) {
                                XYItemEntity entity = (XYItemEntity) chartEntity;
                                int serindex = entity.getSeriesIndex();
                                int itemindex = entity.getItem();
                                double mobility = 0;
                                mobility = dataset.getYValue(serindex, itemindex);
                                List<Scan> selectedScan = imsTask.getSelectedScans();
                                for (int i = 0; i < imsTask.getScans().length; i++) {
                                    if (selectedScan.get(i).getMobility() == mobility && selectedRetentionTime == selectedScan.get(i).getRetentionTime()) {
                                        break;
                                    }
                                }
                                implot.showSelectedScan();
                            }
                        }
                    }

                    @Override
                    public void chartMouseMoved(ChartMouseEventFX event) {
                    }
                });

    }

    void setPixelRenderer(double[] copyXValues, double[] copyYValues, LookupPaintScale scale) {
        pixelRenderer = new XYBlockPixelSizeRenderer();
        pixelRenderer.setPaintScale(scale);

        // set the block renderer renderer
        blockRenderer = new XYBlockRenderer();
        double mzWidth = 0.0;
        double mobilityWidth = 0.0;

        for (int i = 0; i + 1 < copyYValues.length; i++) {
            if (copyYValues[i] != copyYValues[i + 1]) {
                mobilityWidth = copyYValues[i + 1] - copyYValues[i];
                break;
            }
        }
        ArrayList<Double> deltas = new ArrayList<>();
        for (int i = 0; i + 1 < copyXValues.length; i++) {
            if (copyXValues[i] != copyXValues[i + 1]) {
                deltas.add(copyXValues[i + 1] - copyXValues[i]);
            }
        }

        Collections.sort(deltas);
        mzWidth = deltas.get(deltas.size() / 2);

        if (mobilityWidth <= 0.0 || mzWidth <= 0.0) {
            throw new IllegalArgumentException(
                    "there must be atleast two unique value of retentio time and mobility");
        }

        blockRenderer.setBlockHeight(mobilityWidth);
        blockRenderer.setBlockWidth(mzWidth);
    }

    public void prepareLegend(double min, double max, LookupPaintScale scale) {
        NumberAxis scaleAxis = new NumberAxis("Intensity");
        scaleAxis.setRange(min, max);
        scaleAxis.setAxisLinePaint(Color.white);
        scaleAxis.setTickMarkPaint(Color.white);
        legend = new PaintScaleLegend(scale, scaleAxis);

        legend.setStripOutlineVisible(false);
        legend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        legend.setAxisOffset(5.0);
        legend.setMargin(new RectangleInsets(5, 5, 5, 5));
        legend.setFrame(new BlockBorder(Color.white));
        legend.setPadding(new RectangleInsets(10, 10, 10, 10));
        legend.setStripWidth(10);
        legend.setPosition(RectangleEdge.RIGHT);
        legend.getAxis().setLabelFont(legendFont);
        legend.getAxis().setTickLabelFont(legendFont);
    }

}
