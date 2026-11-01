/**
 * RPGM Workbench
 * Shared application JavaScript.
 */

"use strict";


/**
 * Updates the copyright year in the shared application footer.
 */
function updateCopyrightYear() {

    const yearSpan = document.getElementById("year");

    if (yearSpan) {
        yearSpan.textContent = new Date().getFullYear();
    }
}


/**
 * Enables the RPGM logo parallax effect on the Home page.
 */
function initializeLogoParallax() {

    const prefersReducedMotion =
        window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    const logo = document.querySelector(".rpgm-logo");

    if (!logo || prefersReducedMotion) {
        return;
    }

    window.addEventListener("scroll", function() {

        const offset = window.scrollY * 0.12;

        logo.style.transform = `translateY(${offset}px)`;
    });
}


/* Application initialization */
updateCopyrightYear();
initializeLogoParallax();