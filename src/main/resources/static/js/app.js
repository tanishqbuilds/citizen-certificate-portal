/* GetDocs — progressive enhancement. Every page works without this file. */
(function () {
  "use strict";

  var root = document.documentElement;
  var THEME_KEY = "ccrp-theme";

  /* ---------- Theme: light / dark / system ---------- */
  function readTheme() {
    try { return localStorage.getItem(THEME_KEY) || "system"; } catch (e) { return "system"; }
  }
  function applyTheme(mode) {
    if (mode === "light" || mode === "dark") root.setAttribute("data-theme", mode);
    else root.removeAttribute("data-theme");
    document.querySelectorAll("[data-theme-set]").forEach(function (btn) {
      btn.setAttribute("aria-pressed", String(btn.getAttribute("data-theme-set") === mode));
    });
  }
  applyTheme(readTheme());
  document.addEventListener("click", function (e) {
    var btn = e.target.closest("[data-theme-set]");
    if (!btn) return;
    var mode = btn.getAttribute("data-theme-set");
    try { localStorage.setItem(THEME_KEY, mode); } catch (err) { /* private mode */ }
    applyTheme(mode);
  });

  /* ---------- Liquid glass: refraction where the engine supports SVG backdrop filters ---------- */
  try {
    if (window.CSS && CSS.supports("backdrop-filter", "url(#liquid-lens)") && /Chrome|Chromium|Edg\//.test(navigator.userAgent)) {
      root.classList.add("has-liquid");
    }
  } catch (e) { /* ignore */ }

  /* ---------- Pointer-tracked sheen on glass surfaces ---------- */
  var pending = null;
  document.addEventListener("pointermove", function (e) {
    var el = e.target.closest && e.target.closest(".glass, .btn-glass");
    if (!el) return;
    if (pending) cancelAnimationFrame(pending);
    pending = requestAnimationFrame(function () {
      var r = el.getBoundingClientRect();
      el.style.setProperty("--mx", (e.clientX - r.left) + "px");
      el.style.setProperty("--my", (e.clientY - r.top) + "px");
    });
  }, { passive: true });

  /* ---------- Scroll reveal ---------- */
  if ("IntersectionObserver" in window) {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) {
        if (en.isIntersecting) { en.target.classList.add("in"); io.unobserve(en.target); }
      });
    }, { rootMargin: "0px 0px -8% 0px" });
    document.querySelectorAll("[data-reveal]").forEach(function (el) { io.observe(el); });
  } else {
    document.querySelectorAll("[data-reveal]").forEach(function (el) { el.classList.add("in"); });
  }

  /* ---------- Copy to clipboard ---------- */
  document.addEventListener("click", function (e) {
    var btn = e.target.closest("[data-copy]");
    if (!btn || !navigator.clipboard) return;
    navigator.clipboard.writeText(btn.getAttribute("data-copy")).then(function () {
      var label = btn.querySelector("[data-copy-label]");
      if (!label) return;
      var prev = label.textContent;
      label.textContent = "Copied";
      setTimeout(function () { label.textContent = prev; }, 1600);
    });
  });

  /* ---------- Application form ---------- */
  var form = document.querySelector("[data-apply-form]");
  if (form) {
    var typeInputs = form.querySelectorAll("input[name=certificateType]");
    var summaryType = document.querySelector("[data-summary-type]");
    var summaryDays = document.querySelector("[data-summary-sla]");
    var summaryDoc = document.querySelector("[data-summary-doc]");

    function syncType() {
      var checked = form.querySelector("input[name=certificateType]:checked");
      var value = checked ? checked.value : "";
      form.querySelectorAll("[data-show-for]").forEach(function (el) {
        var show = el.getAttribute("data-show-for").split(",").indexOf(value) !== -1;
        el.classList.toggle("is-hidden", !show);
        el.querySelectorAll("input,select,textarea").forEach(function (i) { i.disabled = !show; });
      });
      if (checked && summaryType) {
        summaryType.textContent = checked.getAttribute("data-label");
        summaryDays.textContent = checked.getAttribute("data-sla") + " working days";
        summaryDoc.textContent = checked.getAttribute("data-doc");
      }
    }
    typeInputs.forEach(function (i) { i.addEventListener("change", syncType); });
    syncType();

    /* Character counters */
    form.querySelectorAll("[data-count]").forEach(function (ta) {
      var out = form.querySelector("[data-count-for='" + ta.name + "']");
      var max = ta.getAttribute("maxlength");
      function upd() { if (out) out.textContent = ta.value.length + " / " + max; }
      ta.addEventListener("input", upd); upd();
    });

    /* Drop zone with client-side pre-checks (the server re-validates everything) */
    var dz = form.querySelector("[data-dropzone]");
    if (dz) {
      var input = dz.querySelector("input[type=file]");
      var nameEl = dz.querySelector("[data-file-name]");
      var errEl = document.querySelector("[data-file-error]");
      var maxBytes = parseInt(dz.getAttribute("data-max-bytes"), 10);
      var allowed = ["application/pdf", "image/png", "image/jpeg"];

      ["dragenter", "dragover"].forEach(function (ev) { dz.addEventListener(ev, function () { dz.classList.add("is-drag"); }); });
      ["dragleave", "drop"].forEach(function (ev) { dz.addEventListener(ev, function () { dz.classList.remove("is-drag"); }); });

      input.addEventListener("change", function () {
        var f = input.files && input.files[0];
        dz.classList.remove("has-file");
        if (errEl) { errEl.textContent = ""; errEl.classList.add("is-hidden"); }
        if (!f) return;
        var problem = null;
        if (allowed.indexOf(f.type) === -1) problem = "Only PDF, PNG or JPEG files are accepted.";
        else if (f.size > maxBytes) problem = "File is " + (f.size / 1048576).toFixed(1) + " MB — the limit is " + (maxBytes / 1048576) + " MB.";
        if (problem && errEl) { errEl.textContent = problem; errEl.classList.remove("is-hidden"); return; }
        nameEl.textContent = f.name + " · " + (f.size < 1024 * 1024 ? Math.max(1, Math.round(f.size / 1024)) + " KB" : (f.size / 1048576).toFixed(1) + " MB");
        dz.classList.add("has-file");
      });
    }

    form.addEventListener("submit", function () {
      var btn = form.querySelector("button[type=submit]");
      if (btn) { btn.setAttribute("aria-busy", "true"); btn.querySelector("[data-btn-label]").textContent = "Submitting…"; }
    });
  }

  /* ---------- Clickable table rows ---------- */
  document.addEventListener("click", function (e) {
    var row = e.target.closest("tr[data-href]");
    if (row && !e.target.closest("a,button")) window.location.href = row.getAttribute("data-href");
  });

  /* ---------- Keyboard: "/" focuses search ---------- */
  document.addEventListener("keydown", function (e) {
    if (e.key !== "/" || /input|textarea|select/i.test(document.activeElement.tagName)) return;
    var s = document.querySelector("[data-search]");
    if (s) { e.preventDefault(); s.focus(); }
  });
})();
