package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.Chart;
import com.github.appreciated.apexcharts.config.Fill;
import com.github.appreciated.apexcharts.config.Markers;
import com.github.appreciated.apexcharts.config.PlotOptions;
import com.github.appreciated.apexcharts.config.Stroke;
import com.github.appreciated.apexcharts.config.XAxis;
import com.github.appreciated.apexcharts.config.YAxis;
import com.github.appreciated.apexcharts.config.chart.Toolbar;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.DropShadow; // Importante para el glow
import com.github.appreciated.apexcharts.config.plotoptions.Radar;
import com.github.appreciated.apexcharts.config.plotoptions.radar.Polygons;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.github.appreciated.apexcharts.config.xaxis.Labels;
import com.github.appreciated.apexcharts.config.xaxis.labels.Style;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CombatRadarWidget extends VerticalLayout {

    private final H1 pcValue;
    private final Span pcLabel;
    private final Div chartContainer;

    // COLORES VIBRANTES (NEÓN / LÁSER)
    private static final String COLOR_TEXTO_NUCLEO = "#D50000"; // Rojo Puro Intenso
    private static final String COLOR_RADAR_BORDE  = "#FF3D00"; // Naranja Neón (Lava)
    private static final String COLOR_RADAR_RELLENO = "#FF9100"; // Naranja Brillante

    public CombatRadarWidget() {
        setAlignItems(Alignment.CENTER);
        setPadding(false);
        setSpacing(false);
        getStyle().set("position", "relative").set("margin-top", "20px");

        // --- NÚCLEO ARDIENTE ---
        pcValue = new H1("0");
        pcValue.getStyle()
                .set("font-size", "5.5rem") // Más grande aún
                .set("margin", "0")
                .set("line-height", "1")
                .set("color", COLOR_TEXTO_NUCLEO)
                // EL SECRETO: Doble sombra para efecto de calor irradiando
                .set("text-shadow", "0 0 10px rgba(255, 61, 0, 0.5), 0 0 20px rgba(255, 61, 0, 0.3)")
                .set("font-weight", "900")
                .set("letter-spacing", "-2px");

        pcLabel = new Span();
        pcLabel.getStyle()
                .set("font-size", "1.1rem")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "4px")
                .set("font-weight", "bold")
                .set("color", "#FF3D00") // Texto naranja vibrante
                .set("opacity", "0.9");

        chartContainer = new Div();
        chartContainer.setWidth("100%");
        chartContainer.setMaxWidth("500px");
        chartContainer.setHeight("350px");

        add(pcValue, pcLabel, chartContainer);
    }

    public void updateData(Double poder, Map<String, Double> componentes,
                           String labelPoder, String labelSerie, String msgSinDatos) { // <--- Nuevo parámetro
        pcValue.setText(String.format("%.0f", poder != null ? poder : 0.0));
        pcLabel.setText(labelPoder);

        chartContainer.removeAll();
        if (componentes == null || componentes.isEmpty()) {
            chartContainer.add(new Paragraph(msgSinDatos)); // <--- Uso del texto traducido
        } else {
            chartContainer.add(crearGraficoRadar(componentes, labelSerie));
        }
    }

    private ApexCharts crearGraficoRadar(Map<String, Double> data, String serieLabel) {
        String[] categorias = data.keySet().toArray(new String[0]);
        Double[] valores = data.values().toArray(new Double[0]);

        Markers markers = new Markers();
        markers.setSize(new Double[]{0.0});

        DropShadow dropShadow = new DropShadow();
        dropShadow.setEnabled(true);
        dropShadow.setTop(0.0);
        dropShadow.setLeft(0.0);
        dropShadow.setBlur(6.0);
        dropShadow.setOpacity(0.5);

        Toolbar toolbar = new Toolbar();
        toolbar.setShow(false);

        Chart chartConfig = new Chart();
        chartConfig.setType(Type.RADAR);
        chartConfig.setHeight("350px");
        chartConfig.setToolbar(toolbar);
        chartConfig.setDropShadow(dropShadow);

        Fill fill = new Fill();
        fill.setOpacity(0.7);
        fill.setColors(List.of(COLOR_RADAR_RELLENO));

        Stroke stroke = new Stroke();
        stroke.setWidth(4.0);
        stroke.setColors(List.of(COLOR_RADAR_BORDE));

        // MEJORA 1: Polígonos (Mantenemos tu código actual)
        Polygons polygons = new Polygons();
        polygons.setStrokeColor(List.of("#78909C"));
        polygons.setConnectorColors(List.of("#78909C"));

        Radar radar = new Radar();
        radar.setPolygons(polygons);

        PlotOptions plotOptions = new PlotOptions();
        plotOptions.setRadar(radar);

        // --- CORRECCIÓN ETIQUETAS EJE X ---
        XAxis xaxis = new XAxis();
        xaxis.setCategories(List.of(categorias));

        Labels labels = new Labels();
        Style style = new Style();

        // EL TRUCO: Creamos una lista con el color oscuro repetido N veces
        // donde N es la cantidad de categorías (Fuerza, Velocidad, etc.)
        int totalCategorias = categorias.length;
        style.setColors(Collections.nCopies(totalCategorias, "#37474F"));

        style.setFontSize("14px");

        labels.setStyle(style);
        xaxis.setLabels(labels);
        // ----------------------------------

        YAxis yaxis = new YAxis();
        yaxis.setShow(false);
        yaxis.setMin(0.0);
        yaxis.setMax(5.0);

        return ApexChartsBuilder.get()
                .withChart(chartConfig)
                .withColors(COLOR_RADAR_BORDE)
                .withFill(fill)
                .withStroke(stroke)
                .withMarkers(markers)
                .withPlotOptions(plotOptions)
                .withSeries(new Series<>(serieLabel, valores))
                .withXaxis(xaxis)
                .withYaxis(yaxis)
                .build();
    }
}