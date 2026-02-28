package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

@Tag("div")
public class TabataComponent extends VerticalLayout {

    private H1 timerDisplay;
    private Div colorBackground;

    public TabataComponent() {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background-color", "#222"); // Fondo oscuro inicial
        setSizeFull();

        colorBackground = new Div();
        colorBackground.setSizeFull();
        colorBackground.getStyle().set("position", "absolute");
        colorBackground.getStyle().set("z-index", "0");

        timerDisplay = new H1("00:00");
        timerDisplay.getStyle().set("font-size", "15vw");
        timerDisplay.getStyle().set("color", "white");
        timerDisplay.getStyle().set("z-index", "1");

        add(colorBackground, timerDisplay);
    }

    // Método para disparar el cronómetro desde Java hacia el Navegador con AUDIO INTEGRADO
    public void iniciarTabata(int trabajo, int descanso, int series) {
        getElement().executeJs(
                // 1. Definimos la función de sonido una sola vez
                "const audioCtx = new (window.AudioContext || window.webkitAudioContext)(); " +
                        "const playSound = (freq, duration) => { " +
                        "  const osc = audioCtx.createOscillator(); " +
                        "  const gain = audioCtx.createGain(); " +
                        "  osc.connect(gain); gain.connect(audioCtx.destination); " +
                        "  osc.frequency.value = freq; osc.start(); " +
                        "  setTimeout(() => osc.stop(), duration); " +
                        "}; " +

                        "let t = $0, d = $1, s = $2; " +
                        "let currentS = 1; " +
                        "let isWork = true; " +
                        "let timeLeft = t; " +

                        // Pitazo inicial de HAJIME
                        "playSound(880, 500); " +
                        "document.body.style.backgroundColor = 'green'; " +

                        "let interval = setInterval(() => { " +
                        "  if(timeLeft <= 0) { " +
                        "    if(isWork) { " +
                        "      isWork = false; timeLeft = d; " +
                        "      document.body.style.backgroundColor = 'red'; " +
                        "      playSound(440, 800); // MATE: Pitazo grave al empezar descanso " +
                        "    } else { " +
                        "      isWork = true; timeLeft = t; currentS++; " +
                        "      if(currentS <= s) { " +
                        "        document.body.style.backgroundColor = 'green'; " +
                        "        playSound(880, 500); // HAJIME: Pitazo agudo al empezar serie " +
                        "      } " +
                        "    } " +
                        "    if(currentS > s) { " +
                        "      clearInterval(interval); " +
                        "      document.body.style.backgroundColor = 'white'; " +
                        "      playSound(600, 1500); // SORE MADE: Pitazo final largo " +
                        "      alert('Sore Made! Entrenamiento finalizado'); " +
                        "    } " +
                        "  } " +
                        "  let m = Math.floor(timeLeft / 60); let sec = timeLeft % 60; " +
                        "  $3.innerText = (m<10?'0':'')+m + ':' + (sec<10?'0':'')+sec; " +
                        "  timeLeft--; " +
                        "}, 1000);",
                trabajo, descanso, series, timerDisplay.getElement()
        );
    }
}