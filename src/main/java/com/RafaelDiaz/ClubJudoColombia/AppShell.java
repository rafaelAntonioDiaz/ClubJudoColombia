package com.RafaelDiaz.ClubJudoColombia;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

/**
 * Configuración principal de la PWA y del tema de la aplicación.
 */
@PWA(
        name = "Club Judo Colombia",
        shortName = "JudoCol",
        description = "Gestión deportiva para clubes de judo",
        iconPath = "icons/icon.png",
        backgroundColor = "#227aef",
        themeColor = "#227aef",
        offlinePath = "offline.html",
        offlineResources = { "icons/icon.png" }
)
@Theme("my-theme")  // Ajusta el nombre de tu tema si es otro
public class AppShell implements AppShellConfigurator {
}