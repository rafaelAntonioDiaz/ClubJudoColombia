package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

@Tag("div")
public class TabataComponent extends VerticalLayout {

    private H1 timerDisplay;
    private Span mensajeEstado;
    private Button btnPausar;
    private Button btnDetener;
    private HorizontalLayout controles;
    private Runnable onCloseCallback;

    public TabataComponent(Runnable onCloseCallback) {
        this.onCloseCallback = onCloseCallback;

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        getStyle().set("background-color", "#222");
        getStyle().set("transition", "background-color 0.3s ease");
        setSizeFull();

        mensajeEstado = new Span("PREPARANDO...");
        mensajeEstado.getStyle().set("color", "white");
        mensajeEstado.getStyle().set("font-size", "6vw");
        mensajeEstado.getStyle().set("font-weight", "bold");

        timerDisplay = new H1("00:00");
        timerDisplay.getStyle().set("font-size", "28vw");
        timerDisplay.getStyle().set("color", "white");
        timerDisplay.getStyle().set("margin", "0");
        timerDisplay.getStyle().set("line-height", "1");
        timerDisplay.getStyle().set("cursor", "pointer");

        btnPausar = new Button("PAUSAR", new Icon(VaadinIcon.PAUSE));
        btnPausar.addThemeVariants(ButtonVariant.LUMO_LARGE, ButtonVariant.LUMO_CONTRAST);

        btnDetener = new Button("ABORTAR", new Icon(VaadinIcon.STOP));
        btnDetener.addThemeVariants(ButtonVariant.LUMO_LARGE, ButtonVariant.LUMO_ERROR);

        controles = new HorizontalLayout(btnPausar, btnDetener);
        controles.setSpacing(true);
        controles.getStyle().set("margin-top", "20px");

        // SOLUCIÓN 1: Usamos CSS para ocultar, no setVisible(false) de Vaadin
        controles.getStyle().set("display", "none");

        add(mensajeEstado, timerDisplay, controles);

        // Envía comandos simples desde los botones
        btnPausar.addClickListener(e -> getElement().executeJs("if(this.$tabata) this.$tabata.togglePause();"));
        btnDetener.addClickListener(e -> {
            getElement().executeJs("if(this.$tabata) this.$tabata.stop();");
            if (this.onCloseCallback != null) this.onCloseCallback.run();
        });
    }

    public void iniciarTabata(int trabajo, int descanso, int series) {
        int m = trabajo / 60; int sec = trabajo % 60;
        timerDisplay.setText(String.format("%02d:%02d", m, sec));
        mensajeEstado.setText("TOCA LA PANTALLA PARA INICIAR");

        String jsCode =
                "const el = $0; const display = $1; const texto = $2; const boxControles = $3;" +
                        "el.$tabata = {" +
                        "  t: $4, d: $5, s: $6, " +
                        "  timeLeft: $4, isWork: true, currentS: 1, " +
                        "  interval: null, started: false, paused: false, audioCtx: null," +

                        "  initAudio: function() {" +
                        "    try { this.audioCtx = new (window.AudioContext || window.webkitAudioContext)(); } catch(e){}" +
                        "  }," +
                        "  playSound: function(freq, duration) {" +
                        "    if(!this.audioCtx) return;" +
                        "    try {" +
                        "      const osc = this.audioCtx.createOscillator();" +
                        "      const gain = this.audioCtx.createGain();" +
                        "      osc.connect(gain); gain.connect(this.audioCtx.destination);" +
                        "      osc.frequency.value = freq; osc.start();" +
                        "      setTimeout(() => osc.stop(), duration);" +
                        "    } catch(e) {}" +
                        "  }," +

                        "  start: function() {" +
                        "    this.started = true;" +
                        "    this.initAudio();" +
                        "    boxControles.style.display = 'flex';" + // Revela los botones
                        "    this.updateUI('TRABAJO (1/' + this.s + ')', '#2e7d32');" +
                        "    this.playSound(880, 500);" +
                        "    this.resume();" +
                        "  }," +

                        "  togglePause: function() {" +
                        "    if(!this.started) return;" +
                        "    if(this.paused) this.resume(); else this.pause();" +
                        "  }," +

                        "  pause: function() {" +
                        "    this.paused = true;" +
                        "    clearInterval(this.interval);" +
                        "    el.style.backgroundColor = '#f57f17';" +
                        "    texto.innerText = 'PAUSADO';" +
                        "  }," +

                        "  resume: function() {" +
                        "    this.paused = false;" +
                        "    el.style.backgroundColor = this.isWork ? '#2e7d32' : '#c62828';" +
                        "    texto.innerText = this.isWork ? 'TRABAJO (' + this.currentS + '/' + this.s + ')' : 'DESCANSO';" +
                        "    this.interval = setInterval(() => this.tick(), 1000);" +
                        "  }," +

                        "  stop: function() {" +
                        "    clearInterval(this.interval);" +
                        "  }," +

                        "  tick: function() {" +
                        "    if(!document.body.contains(el)) { this.stop(); return; }" + // Seguro anti-fantasmas
                        "    if(this.timeLeft <= 0) {" +
                        "      if(this.isWork) {" +
                        "        this.isWork = false; this.timeLeft = this.d;" +
                        "        this.updateUI('DESCANSO', '#c62828');" +
                        "        this.playSound(440, 800);" +
                        "      } else {" +
                        "        this.isWork = true; this.timeLeft = this.t; this.currentS++;" +
                        "        if(this.currentS <= this.s) {" +
                        "          this.updateUI('TRABAJO (' + this.currentS + '/' + this.s + ')', '#2e7d32');" +
                        "          this.playSound(880, 500);" +
                        "        }" +
                        "      }" +
                        "      if(this.currentS > this.s) {" +
                        "        this.stop();" +
                        "        this.updateUI('SORE MADE', '#1565c0');" +
                        "        display.innerText = '00:00';" +
                        "        this.playSound(600, 1500);" +
                        "        return;" +
                        "      }" +
                        "    }" +
                        "    let m = Math.floor(this.timeLeft / 60); let s = this.timeLeft % 60;" +
                        "    display.innerText = (m<10?'0':'')+m + ':' + (s<10?'0':'')+s;" +
                        "    this.timeLeft--;" +
                        "  }," +

                        "  updateUI: function(txt, color) {" +
                        "    texto.innerText = txt; el.style.backgroundColor = color;" +
                        "  }" +
                        "};" +

                        // SOLUCIÓN 2: Capturamos el clic directamente en el navegador, no pasa por Java
                        "el.addEventListener('click', function(e) {" +
                        "  if(!el.$tabata.started) el.$tabata.start();" +
                        "});";

        getElement().executeJs(jsCode,
                getElement(), timerDisplay.getElement(), mensajeEstado.getElement(), controles.getElement(),
                trabajo, descanso, series);
    }
}