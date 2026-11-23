/* RepVault site */
(function () {
  var $ = function (id) { return document.getElementById(id); };
  var R = window.RV, reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;
  function esc(s) { return String(s == null ? "" : s).replace(/[&<>"]/g, function (c) { return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]; }); }
  function store(k, v) { try { if (v === undefined) return localStorage.getItem(k); if (v === null) localStorage.removeItem(k); else localStorage.setItem(k, v); } catch (e) { return null; } }
  function kg(w) { return (Math.round(w * 100) / 100).toString(); }
  function big(n) { return n >= 1e6 ? (n / 1e6).toFixed(2) + "M" : n >= 1e4 ? (n / 1e3).toFixed(1) + "k" : Math.round(n).toLocaleString(); }
  function fmtDate(ms) { return new Date(ms).toLocaleDateString(undefined, { month: "short", day: "numeric" }); }

  /* ---------- data: demo history + what the visitor logs ---------- */
  var NOW = Date.now(), demo = R.demoHistory(NOW);
  function mine() { try { return JSON.parse(store("rv-site-log") || '{"sessions":[],"sets":[]}'); } catch (e) { return { sessions: [], sets: [] }; } }
  function db() { var m = mine(); return { sessions: demo.sessions.concat(m.sessions), sets: demo.sets.concat(m.sets) }; }

  /* ---------- tab icon: the RepVault bars, lit as you log ---------- */
  var baseTitle = document.title;
  function setIcon(lit, live, title) {
    var heights = [10, 14, 18, 24], x0 = 17, w = 6.4, gap = 2.6, svg = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64"><rect x="2" y="2" width="60" height="60" rx="15" fill="#0b0b0c"/>';
    heights.forEach(function (h, i) { svg += '<rect x="' + (x0 + i * (w + gap)) + '" y="' + (46 - h) + '" width="' + w + '" height="' + h + '" rx="2" fill="' + (i < lit ? "#5fd472" : "#f4f4f5") + '"/>'; });
    if (live) svg += '<circle cx="51" cy="13" r="7.5" fill="#5fd472" stroke="#0b0b0c" stroke-width="3"/>';
    svg += "</svg>";
    $("favicon").href = lit || live ? "data:image/svg+xml," + encodeURIComponent(svg) : "favicon.svg";
    document.title = title || baseTitle;
  }

  /* ---------- confetti ---------- */
  var cv = $("confetti"), cx2 = cv.getContext("2d"), bits = [], confRun = false;
  function confetti(x, y, n, colors) {
    if (reduced) return;
    var dpr = Math.min(2, devicePixelRatio || 1); cv.width = innerWidth * dpr; cv.height = innerHeight * dpr; cx2.setTransform(dpr, 0, 0, dpr, 0, 0);
    colors = colors || ["#d83a2e", "#2f63d6", "#e8b51e", "#2f9e4a", "#f2f2f2"];
    for (var i = 0; i < n; i++) { var a = Math.random() * Math.PI * 2, s = 3 + Math.random() * 7; bits.push({ x: x, y: y, vx: Math.cos(a) * s, vy: Math.sin(a) * s - 5, r: 3 + Math.random() * 4, c: colors[i % colors.length], life: 60 + Math.random() * 40, rot: Math.random() * 6 }); }
    if (!confRun) { confRun = true; requestAnimationFrame(stepConf); }
  }
  function stepConf() {
    cx2.clearRect(0, 0, innerWidth, innerHeight);
    bits = bits.filter(function (b) { return b.life > 0; });
    bits.forEach(function (b) { b.x += b.vx; b.y += b.vy; b.vy += .28; b.vx *= .99; b.life--; b.rot += .2; cx2.save(); cx2.translate(b.x, b.y); cx2.rotate(b.rot); cx2.globalAlpha = Math.min(1, b.life / 30); cx2.fillStyle = b.c; cx2.fillRect(-b.r, -b.r / 2, b.r * 2, b.r); cx2.restore(); });
    if (bits.length) requestAnimationFrame(stepConf); else { confRun = false; cx2.clearRect(0, 0, innerWidth, innerHeight); }
  }

  /* ---------- hero rack ---------- */
  var tile = $("rack-tile"), lifts = tile.querySelectorAll(".lift"), heroReps = 0, heroSets = 0;
  lifts.forEach(function (b) {
    b.addEventListener("click", function () {
      heroReps++; var k = (heroReps - 1) % 4;
      b.classList.remove("pop"); void b.offsetWidth; b.classList.add("pop");
      lifts[k].classList.add("done");
      $("rack-reps").textContent = heroReps;
      setIcon(k + 1, false, "● " + heroReps + " reps, RepVault");
      if (k === 3) {
        heroSets++;
        var burst = document.createElement("span"); burst.className = "burst"; burst.textContent = "SET " + heroSets; burst.style.left = "40%"; burst.style.top = "30%";
        tile.appendChild(burst); setTimeout(function () { burst.remove(); }, 1000);
        var r = tile.getBoundingClientRect(); confetti(r.left + r.width / 2, r.top + r.height * .45, 40);
        $("rack-hint").textContent = heroSets === 1 ? "One set down. Ready for a real session? Scroll down." : heroSets + " sets. Your shoulders are fine, it is a website.";
        setTimeout(function () { lifts.forEach(function (l) { l.classList.remove("done"); }); setIcon(0, false); }, 650);
      }
    });
  });
  $("rack").addEventListener("pointermove", function (e) {
    if (reduced) return; var r = this.getBoundingClientRect(), x = (e.clientX - r.left) / r.width - .5, y = (e.clientY - r.top) / r.height - .5;
    tile.style.transform = "rotateY(" + (x * 16).toFixed(2) + "deg) rotateX(" + (-y * 16).toFixed(2) + "deg)";
  });
  $("rack").addEventListener("pointerleave", function () { tile.style.transform = ""; });

  /* ---------- the session logger ---------- */
  var cur = JSON.parse(store("rv-site-current") || "null") || { type: "Push", exercises: R.TEMPLATES[0].exercises.map(function (e) { return e.id; }), sets: [], active: R.TEMPLATES[0].exercises[0].id, start: null };
  var dial = {};
  function saveCur() { store("rv-site-current", JSON.stringify(cur)); }
  function ex(id) { return R.EXERCISES[id - 1]; }
  function stepFor(e) { var k = R.kind(e.name); return k === "dumbbell" ? 1 : k === "bodyweight" ? 2.5 : 2.5; }
  function bestBefore(exId) { var d = db(), m = 0; d.sets.forEach(function (s) { if (s.exerciseId === exId && s.weight > m) m = s.weight; }); return m; }
  function initDial(exId) {
    if (dial[exId]) return dial[exId];
    var prev = R.previousFor(db(), exId), k = R.kind(ex(exId).name);
    var w = prev ? Math.max.apply(null, prev.sets.map(function (s) { return s.weight; })) : k === "barbell" ? 40 : k === "bodyweight" ? 0 : 10;
    var reps = prev ? prev.sets[0].reps : 8;
    return (dial[exId] = { w: w, reps: reps });
  }

  $("templates").innerHTML = R.TEMPLATES.map(function (t) { return '<button type="button" data-t="' + t.name + '">' + t.name + '</button>'; }).join("");
  $("templates").addEventListener("click", function (e) {
    var b = e.target.closest("button"); if (!b) return;
    var t = R.TEMPLATES.filter(function (x) { return x.name === b.dataset.t; })[0];
    var keep = cur.exercises.filter(function (id) { return cur.sets.some(function (s) { return s.exerciseId === id; }); });
    cur.type = t.name; cur.exercises = keep.concat(t.exercises.map(function (x) { return x.id; }).filter(function (id) { return keep.indexOf(id) < 0; }));
    cur.active = cur.exercises[0] || null; saveCur(); renderFloor();
  });
  var groups = {}; R.EXERCISES.forEach(function (e) { (groups[e.muscleGroup] = groups[e.muscleGroup] || []).push(e); });
  $("add-ex").innerHTML = '<option value="">Add an exercise</option>' + Object.keys(groups).map(function (g) { return '<optgroup label="' + g + '">' + groups[g].map(function (e) { return '<option value="' + e.id + '">' + esc(e.name) + '</option>'; }).join("") + '</optgroup>'; }).join("");
  $("add-ex").addEventListener("change", function () { var id = +this.value; if (!id) return; if (cur.exercises.indexOf(id) < 0) cur.exercises.push(id); cur.active = id; this.value = ""; saveCur(); renderFloor(); });
  $("ex-list").addEventListener("click", function (e) { var b = e.target.closest("button"); if (!b) return; cur.active = +b.dataset.id; saveCur(); renderFloor(); });

  function renderFloor() {
    document.querySelectorAll("#templates button").forEach(function (b) { b.setAttribute("aria-pressed", String(b.dataset.t === cur.type)); });
    $("ex-list").innerHTML = cur.exercises.map(function (id) {
      var n = cur.sets.filter(function (s) { return s.exerciseId === id; }).length;
      return '<li><button type="button" data-id="' + id + '" aria-current="' + (id === cur.active) + '"><span>' + esc(ex(id).name) + '</span><span class="cnt' + (n ? " on" : "") + '">' + (n || "") + '</span></button></li>';
    }).join("") || '<li class="note" style="padding:8px">Custom day: add exercises below.</li>';
    renderStation(); renderSummary();
  }

  function barbellSvg(w, kind) {
    var W = 640, H = 170, cy = 85;
    if (kind === "bodyweight") return '<svg viewBox="0 0 ' + W + ' 120" role="img" aria-label="Bodyweight exercise"><text x="' + W / 2 + '" y="66" text-anchor="middle" font-family="var(--display)" font-weight="900" font-size="44" fill="var(--ink-3)">BODYWEIGHT' + (w ? " + " + kg(w) + " KG" : "") + '</text></svg>';
    if (kind === "machine") {
      var plates = Math.max(1, Math.min(14, Math.round(w / 5))), s = '<svg viewBox="0 0 ' + W + ' ' + H + '" role="img" aria-label="Weight stack at ' + kg(w) + ' kilograms">';
      for (var i = 0; i < 14; i++) { var y = 18 + i * 10, on = i >= 14 - plates; s += '<rect class="plate" x="250" y="' + y + '" width="140" height="8" rx="2" fill="' + (on ? "var(--ink)" : "var(--line)") + '" style="animation-delay:' + (i * 15) + 'ms"/>'; }
      s += '<rect x="316" y="6" width="8" height="160" rx="3" fill="var(--ink-3)"/><circle cx="404" cy="' + (18 + (14 - plates) * 10 + 4) + '" r="7" fill="var(--pr)"/>';
      return s + '<text x="430" y="' + (18 + (14 - plates) * 10 + 9) + '" font-family="var(--display)" font-weight="900" font-size="26" fill="var(--ink)">' + kg(w) + ' kg</text></svg>';
    }
    if (kind === "dumbbell") {
      var hs = Math.min(1, .45 + w / 60), d = '<svg viewBox="0 0 ' + W + ' ' + H + '" role="img" aria-label="Dumbbell, ' + kg(w) + ' kilograms">';
      d += '<rect x="230" y="' + (cy - 6) + '" width="180" height="12" rx="4" fill="var(--ink-3)"/>';
      [[200, 1], [410, -1]].forEach(function (p) { var bw = 26 + hs * 14, bh = 60 + hs * 60; d += '<rect class="plate" x="' + (p[1] > 0 ? p[0] - bw + 30 : p[0]) + '" y="' + (cy - bh / 2) + '" width="' + bw + '" height="' + bh + '" rx="8" fill="var(--ink)"/>'; });
      return d + '<text x="' + W / 2 + '" y="' + (cy - 20) + '" text-anchor="middle" font-family="var(--display)" font-weight="900" font-size="28" fill="var(--ink)">' + kg(w) + ' kg</text></svg>';
    }
    var calc = R.plates(w, 20), sz = { 25: [150, 22, "--p25"], 20: [150, 20, "--p20"], 15: [132, 18, "--p15"], 10: [114, 16, "--p10"], 5: [86, 13, "--p5"], 2.5: [66, 10, "--p25s"], 1.25: [54, 8, "--p125"] };
    var out = '<svg viewBox="0 0 ' + W + ' ' + H + '" role="img" aria-label="Barbell loaded to ' + kg(w) + ' kilograms: ' + (calc.plates.length ? calc.plates.join(", ") + " per side" : "empty bar") + '">';
    out += '<rect x="20" y="' + (cy - 5) + '" width="600" height="10" rx="5" fill="var(--ink-3)"/><rect x="150" y="' + (cy - 9) + '" width="10" height="18" rx="2" fill="var(--ink-2)"/><rect x="480" y="' + (cy - 9) + '" width="10" height="18" rx="2" fill="var(--ink-2)"/>';
    var xl = 148, xr = 492;
    calc.plates.forEach(function (p, i) {
      var s2 = sz[p], h = s2[0], t = s2[1], col = "var(" + s2[2] + ")", stroke = p === 5 ? ' stroke="var(--ink-3)" stroke-width="1"' : "";
      xl -= t + 2; out += '<rect class="plate" x="' + xl + '" y="' + (cy - h / 2) + '" width="' + t + '" height="' + h + '" rx="3" fill="' + col + '"' + stroke + ' style="animation-delay:' + (i * 40) + 'ms"/>';
      out += '<rect class="plate" x="' + xr + '" y="' + (cy - h / 2) + '" width="' + t + '" height="' + h + '" rx="3" fill="' + col + '"' + stroke + ' style="animation-delay:' + (i * 40) + 'ms"/>'; xr += t + 2;
    });
    out += '<text x="' + W / 2 + '" y="' + (cy - 26) + '" text-anchor="middle" font-family="var(--display)" font-weight="900" font-size="30" fill="var(--ink)">' + kg(w) + ' kg</text>';
    out += '<text x="' + W / 2 + '" y="' + (cy + 44) + '" text-anchor="middle" font-family="var(--sans)" font-weight="600" font-size="14" fill="var(--ink-3)">' + (calc.plates.length ? "20 kg bar + " + calc.plates.join(" + ") + " each side" : "Empty 20 kg bar") + (calc.remainder ? ", " + calc.remainder + " kg left over" : "") + '</text>';
    return out + "</svg>";
  }

  function renderStation() {
    var id = cur.active, st = $("station");
    if (!id) { st.innerHTML = '<p class="note">Pick an exercise to start.</p>'; return; }
    var e = ex(id), k = R.kind(e.name), d = initDial(id), prev = R.previousFor(db(), id), mineSets = cur.sets.map(function (s, i) { return Object.assign({ i: i }, s); }).filter(function (s) { return s.exerciseId === id; });
    var h = '<div class="station-top"><div><h3>' + esc(e.name) + '</h3><span class="muscle">' + e.muscleGroup + ', ' + k + '</span></div><span class="chip' + (bestBefore(id) ? "" : " go") + '">best ' + (bestBefore(id) ? kg(bestBefore(id)) + " kg" : "none yet") + '</span></div>';
    h += '<div class="prev">' + (prev ? 'Last time, ' + fmtDate(prev.date) + ': <b>' + prev.sets.map(function (s) { return kg(s.weight) + "×" + s.reps; }).join(", ") + '</b> <button class="btn small" type="button" id="autofill">Auto-fill</button>' : "No previous sets for this one yet.") + '</div>';
    h += '<div class="barbell" id="barbell">' + barbellSvg(d.w, k) + '</div>';
    h += '<div class="steppers"><div class="stepper"><button type="button" data-d="w" data-v="-1" aria-label="Less weight">−</button><label class="val"><input id="w-in" type="number" inputmode="decimal" step="0.25" min="0" value="' + kg(d.w) + '" aria-label="Weight in kilograms"><span>kg</span></label><button type="button" data-d="w" data-v="1" aria-label="More weight">+</button></div>';
    h += '<div class="stepper"><button type="button" data-d="r" data-v="-1" aria-label="Fewer reps">−</button><label class="val"><input id="r-in" type="number" inputmode="numeric" step="1" min="1" value="' + d.reps + '" aria-label="Reps"><span>reps</span></label><button type="button" data-d="r" data-v="1" aria-label="More reps">+</button></div></div>';
    h += '<div class="log-row"><button class="btn go" type="button" id="log-set">Log set ' + (mineSets.length + 1) + '</button></div>';
    h += '<ul class="sets" id="sets">' + mineSets.map(function (s, n) { return '<li><b>' + (n + 1) + '</b><span class="w">' + kg(s.weight) + ' kg × ' + s.reps + '</span>' + (s.pr ? '<span class="chip pr">PR</span>' : '<span></span>') + '<button type="button" data-del="' + s.i + '" aria-label="Delete set ' + (n + 1) + '">×</button></li>'; }).join("") + '</ul>';
    st.innerHTML = h;
  }
  $("station").addEventListener("click", function (e) {
    var id = cur.active, d = dial[id], k = R.kind(ex(id).name);
    var s = e.target.closest("[data-d]");
    if (s) {
      if (s.dataset.d === "w") d.w = Math.max(0, Math.round((d.w + (+s.dataset.v) * stepFor(ex(id))) * 100) / 100); else d.reps = Math.max(1, d.reps + (+s.dataset.v));
      $("w-in").value = kg(d.w); $("r-in").value = d.reps; $("barbell").innerHTML = barbellSvg(d.w, k); return;
    }
    if (e.target.closest("#autofill")) {
      var prev = R.previousFor(db(), id); if (!prev) return;
      prev.sets.forEach(function (p) { logSet(id, p.weight, p.reps, true); });
      renderFloor(); return;
    }
    if (e.target.closest("#log-set")) { logSet(id, d.w, d.reps); renderFloor(); return; }
    var del = e.target.closest("[data-del]");
    if (del) { cur.sets.splice(+del.dataset.del, 1); recomputePRs(); saveCur(); renderFloor(); }
  });
  $("station").addEventListener("change", function (e) {
    var d = dial[cur.active];
    if (e.target.id === "w-in") { d.w = Math.max(0, Math.round((+e.target.value || 0) * 100) / 100); $("barbell").innerHTML = barbellSvg(d.w, R.kind(ex(cur.active).name)); }
    if (e.target.id === "r-in") d.reps = Math.max(1, Math.round(+e.target.value || 1));
  });
  function recomputePRs() {
    var seen = {};
    cur.sets.forEach(function (s) { var base = Math.max(bestBefore(s.exerciseId), seen[s.exerciseId] || 0); s.pr = s.weight > 0 && s.weight > base; seen[s.exerciseId] = Math.max(seen[s.exerciseId] || 0, s.weight); });
  }
  function logSet(id, w, reps, quiet) {
    if (!cur.start) cur.start = Date.now();
    var base = Math.max(bestBefore(id), Math.max.apply(null, [0].concat(cur.sets.filter(function (s) { return s.exerciseId === id; }).map(function (s) { return s.weight; }))));
    var pr = w > 0 && w > base;
    cur.sets.push({ exerciseId: id, weight: w, reps: reps, pr: pr }); saveCur();
    if (pr && !quiet) {
      var b = $("station").getBoundingClientRect(); confetti(b.left + b.width / 2, b.top + 160, 70, ["#ffc94a", "#e8b51e", "#f2f2f2", "#5fd472"]);
    }
  }
  function renderSummary() {
    var vol = 0, reps = 0; cur.sets.forEach(function (s) { vol += s.weight * s.reps; reps += s.reps; });
    $("s-sets").textContent = cur.sets.length; $("s-vol").textContent = big(vol); $("s-reps").textContent = reps;
    var prs = cur.sets.filter(function (s) { return s.pr; }), bestPer = {};
    prs.forEach(function (s) { if (!bestPer[s.exerciseId] || s.weight > bestPer[s.exerciseId]) bestPer[s.exerciseId] = s.weight; });
    $("prs").innerHTML = Object.keys(bestPer).map(function (k) { return '<li><span class="chip pr">PR</span>' + esc(ex(+k).name) + ', ' + kg(bestPer[k]) + ' kg</li>'; }).join("");
    $("finish").disabled = !cur.sets.length;
    if (cur.sets.length) setIcon(Math.min(4, cur.sets.length % 4 || 4), true, "● " + cur.type + " day, " + cur.sets.length + (cur.sets.length === 1 ? " set" : " sets") + ", RepVault");
    else setIcon(0, false);
  }
  setInterval(function () { if (!cur.start) { $("s-time").textContent = "0:00"; return; } var s = Math.floor((Date.now() - cur.start) / 1000); $("s-time").textContent = Math.floor(s / 60) + ":" + String(s % 60).padStart(2, "0"); }, 1000);

  $("finish").addEventListener("click", function () {
    if (!cur.sets.length) return;
    var m = mine(), sid = 100000 + m.sessions.length + 1, setBase = 1000000 + m.sets.length, now = Date.now();
    m.sessions.push({ id: sid, date: now, workoutType: cur.type });
    cur.sets.forEach(function (s, i) { m.sets.push({ id: setBase + i, sessionId: sid, exerciseId: s.exerciseId, weight: s.weight, reps: s.reps }); });
    store("rv-site-log", JSON.stringify(m));
    var vol = 0, reps = 0; cur.sets.forEach(function (s) { vol += s.weight * s.reps; reps += s.reps; });
    var prs = $("prs").innerHTML, mins = cur.start ? Math.max(1, Math.round((now - cur.start) / 60000)) : 1;
    $("done-type").textContent = cur.type + " day saved"; $("done-title").textContent = prs ? "New records." : "Session done.";
    $("done-stats").innerHTML = '<div><b>' + cur.sets.length + '</b><span>sets</span></div><div><b>' + big(vol) + '</b><span>kg volume</span></div><div><b>' + reps + '</b><span>reps</span></div><div><b>' + mins + '</b><span>' + (mins === 1 ? "minute" : "minutes") + '</span></div>';
    $("done-prs").innerHTML = prs;
    cur = { type: cur.type, exercises: cur.exercises, sets: [], active: cur.exercises[0] || null, start: null }; dial = {}; saveCur();
    renderFloor(); renderDash(); renderProgress();
    var dlg = $("done"); if (dlg.showModal) dlg.showModal(); else dlg.setAttribute("open", "");
    confetti(innerWidth / 2, innerHeight / 2.5, 140);
  });
  $("done-close").addEventListener("click", function () { $("done").close(); });
  $("done-dash").addEventListener("click", function () { $("done").close(); });

  /* ---------- dashboard ---------- */
  var heatMode = "app";
  function renderDash() {
    var d = db(), t = R.totals(d), now = Date.now(), app = R.currentStreak(d, now), meant = R.intendedStreak(d, now), longest = R.longestStreak(d);
    var week = d.sessions.filter(function (s) { return s.date > R.dayStart(now) - 6 * 864e5; }).length;
    var tiles = [
      ["Current streak", app + (app === 1 ? " day" : " days"), app !== meant ? "As the app computes it." : "Consecutive days with a workout.", app !== meant ? "bug" : "", app !== meant ? "Should read " + meant + ": the last workout was yesterday, but the app only counts from today." : ""],
      ["Longest streak", longest + (longest === 1 ? " day" : " days"), "Back-to-back training days. Any rest day resets it."],
      ["Workouts", d.sessions.length, week + " in the last 7 days"],
      ["Total sets", t.sets.toLocaleString(), "Across every exercise"],
      ["Total volume", big(t.volume) + " kg", "Weight times reps, summed"],
      ["Favourite exercise", t.favorite ? t.favorite.name : "None", "The one with the most sets"],
      ["“Recent PR”", t.pr ? kg(t.pr.weight) + " kg" : "None", t.pr ? t.pr.exercise.name + ". It is the heaviest set ever, on any lift." : ""],
      ["Exercises trained", Object.keys(d.sets.reduce(function (a, s) { a[s.exerciseId] = 1; return a; }, {})).length, "of 63 in the library"]
    ];
    $("dash").innerHTML = tiles.map(function (x) { return '<div class="card tile' + (x[3] ? " " + x[3] : "") + '"><span class="lbl">' + x[0] + '</span><b>' + esc(x[1]) + '</b><span>' + esc(x[2]) + '</span>' + (x[4] ? '<span class="fix"><span class="chip bad">bug</span> ' + esc(x[4]) + '</span>' : "") + '</div>'; }).join("");
    var weeks = R.heatmap(d, now), todayKey = R.ymd(now), myDays = {}; mine().sessions.forEach(function (s) { myDays[R.ymd(s.date)] = 1; });
    var lastLabel = "", h = "", maxVol = 1; weeks.forEach(function (w) { w.days.forEach(function (day) { maxVol = Math.max(maxVol, day.volume); }); });
    weeks.forEach(function (w) {
      h += '<div class="heat-col"><small>' + (w.label !== lastLabel ? w.label : "") + '</small>'; lastLabel = w.label;
      w.days.forEach(function (day) {
        var c = day.sets ? (heatMode === "app" ? "color-mix(in srgb, var(--go) 60%, transparent)" : "color-mix(in srgb, var(--go) " + Math.round(18 + 82 * Math.pow(day.volume / maxVol, 1.6)) + "%, transparent)") : "";
        h += '<span class="cell' + (day.future ? " future" : "") + (myDays[R.ymd(day.date)] ? " new" : "") + '" title="' + new Date(day.date).toDateString() + (day.sets ? ", " + day.sets + " sets, " + Math.round(day.volume).toLocaleString() + " kg" : "") + '"' + (c ? ' style="background:' + c + '"' : "") + '></span>';
      });
      h += "</div>";
    });
    $("heat").innerHTML = h;
    $("heat-note").textContent = heatMode === "app" ? "The heatmap component has three intensity levels, but every active day is given 0.6, so all training days look the same." : "The same calendar with the unused levels put to work, shaded by the volume lifted that day. Leg days stand out. Days you logged here are outlined.";
    void todayKey;
  }
  $("heat-mode").addEventListener("click", function (e) { var b = e.target.closest("button"); if (!b) return; heatMode = b.dataset.m; this.querySelectorAll("button").forEach(function (x) { x.setAttribute("aria-pressed", String(x === b)); }); renderDash(); });

  /* ---------- progress ---------- */
  var progMode = "best", progEx = R.byName["Bench Press"].id;
  function fillProgSelect() {
    var d = db(), trained = {}; d.sets.forEach(function (s) { trained[s.exerciseId] = (trained[s.exerciseId] || 0) + 1; });
    var withData = R.EXERCISES.filter(function (e) { return trained[e.id]; }), rest = R.EXERCISES.filter(function (e) { return !trained[e.id]; });
    $("prog-ex").innerHTML = '<optgroup label="Trained">' + withData.map(function (e) { return '<option value="' + e.id + '">' + esc(e.name) + '</option>'; }).join("") + '</optgroup><optgroup label="Not trained yet">' + rest.map(function (e) { return '<option value="' + e.id + '">' + esc(e.name) + '</option>'; }).join("") + '</optgroup>';
    $("prog-ex").value = progEx;
  }
  function renderProgress() {
    fillProgSelect();
    var st = R.exerciseStats(db(), progEx), hist = st.history, W = 640, H = 300, L = 54, Rr = 16, T = 18, B = 36;
    var vals = hist.map(function (p) { return progMode === "best" ? p.best : p.volume; });
    var s = '<svg viewBox="0 0 ' + W + ' ' + H + '" role="img" aria-label="' + (progMode === "best" ? "Best weight" : "Volume") + ' per session for ' + esc(ex(progEx).name) + '">';
    if (hist.length < 1) s += '<text x="' + W / 2 + '" y="' + H / 2 + '" text-anchor="middle" font-size="16" fill="var(--ink-3)">No sessions yet. Log it in the session above.</text>';
    else {
      var mn = Math.min.apply(null, vals), mx = Math.max.apply(null, vals); if (mx === mn) { mx += 1; mn = Math.max(0, mn - 1); }
      var pad = (mx - mn) * .15; mn = Math.max(0, mn - pad); mx += pad;
      var x = function (i) { return hist.length === 1 ? (L + W - Rr) / 2 : L + i / (hist.length - 1) * (W - L - Rr); }, y = function (v) { return T + (1 - (v - mn) / (mx - mn)) * (H - T - B); };
      for (var g = 0; g <= 4; g++) { var gv = mn + (mx - mn) * g / 4; s += '<line x1="' + L + '" x2="' + (W - Rr) + '" y1="' + y(gv) + '" y2="' + y(gv) + '" stroke="var(--line)"/><text x="' + (L - 8) + '" y="' + (y(gv) + 4) + '" text-anchor="end" font-size="12" fill="var(--ink-3)">' + (progMode === "best" ? kg(Math.round(gv * 2) / 2) : big(gv)) + '</text>'; }
      var pts = vals.map(function (v, i) { return x(i).toFixed(1) + "," + y(v).toFixed(1); });
      s += '<polygon points="' + x(0) + ',' + (H - B) + ' ' + pts.join(" ") + ' ' + x(vals.length - 1) + ',' + (H - B) + '" fill="color-mix(in srgb, var(--go) 14%, transparent)"/>';
      s += '<polyline points="' + pts.join(" ") + '" fill="none" stroke="var(--go)" stroke-width="3" stroke-linejoin="round"/>';
      var bestSoFar = 0;
      hist.forEach(function (p, i) { var v = vals[i], isPr = progMode === "best" && p.best > bestSoFar; if (p.best > bestSoFar) bestSoFar = p.best; s += '<circle cx="' + x(i) + '" cy="' + y(v) + '" r="' + (isPr ? 5.5 : 3.5) + '" fill="' + (isPr ? "var(--pr)" : "var(--surface)") + '" stroke="var(--go)" stroke-width="2"><title>' + fmtDate(p.date) + ': ' + (progMode === "best" ? kg(v) + " kg" : big(v) + " kg") + '</title></circle>'; });
      s += '<text x="' + L + '" y="' + (H - 10) + '" font-size="12" fill="var(--ink-3)">' + fmtDate(hist[0].date) + '</text><text x="' + (W - Rr) + '" y="' + (H - 10) + '" text-anchor="end" font-size="12" fill="var(--ink-3)">' + fmtDate(hist[hist.length - 1].date) + '</text>';
    }
    $("prog-chart").innerHTML = s + '</svg><div class="legend"><span><i style="background:var(--go)"></i>' + (progMode === "best" ? "Heaviest set per session" : "Weight × reps per session") + '</span>' + (progMode === "best" ? '<span><i style="background:var(--pr)"></i>New best</span>' : "") + '</div>';
    $("prog-mini").innerHTML = [["Best", st.bestWeight ? kg(st.bestWeight) + " kg" : "0"], ["Sessions", st.totalSessions], ["Sets", st.totalSets], ["Volume", big(st.totalVolume) + " kg"], ["Avg reps", st.avgReps ? st.avgReps.toFixed(1) : "0"], ["Last trained", st.lastTrained ? fmtDate(st.lastTrained) : "Never"]].map(function (m) { return '<div><b>' + m[1] + '</b><span>' + m[0] + '</span></div>'; }).join("");
  }
  $("prog-ex").addEventListener("change", function () { progEx = +this.value; renderProgress(); });
  $("prog-mode").addEventListener("click", function (e) { var b = e.target.closest("button"); if (!b) return; progMode = b.dataset.m; this.querySelectorAll("button").forEach(function (x) { x.setAttribute("aria-pressed", String(x === b)); }); renderProgress(); });

  /* ---------- library ---------- */
  var libGroup = "All";
  $("groups").innerHTML = ["All"].concat(Object.keys(groups)).map(function (g) { return '<button type="button" data-g="' + g + '" aria-pressed="' + (g === "All") + '">' + g + '<em>' + (g === "All" ? 63 : groups[g].length) + '</em></button>'; }).join("");
  function renderLib() {
    var q = $("lib-q").value.trim().toLowerCase();
    var list = R.EXERCISES.filter(function (e) { return (libGroup === "All" || e.muscleGroup === libGroup) && (!q || e.name.toLowerCase().indexOf(q) >= 0); });
    $("lib").innerHTML = list.map(function (e, i) { return '<button type="button" data-id="' + e.id + '" style="animation-delay:' + Math.min(i * 12, 300) + 'ms"><b>' + esc(e.name) + '</b><span>' + e.muscleGroup + ', ' + R.kind(e.name) + '</span></button>'; }).join("") || '<p class="note">No exercise by that name.</p>';
  }
  $("groups").addEventListener("click", function (e) { var b = e.target.closest("button"); if (!b) return; libGroup = b.dataset.g; this.querySelectorAll("button").forEach(function (x) { x.setAttribute("aria-pressed", String(x === b)); }); renderLib(); });
  $("lib-q").addEventListener("input", renderLib);
  $("lib").addEventListener("click", function (e) { var b = e.target.closest("button"); if (!b) return; progEx = +b.dataset.id; renderProgress(); $("progress").scrollIntoView({ behavior: reduced ? "auto" : "smooth" }); });

  /* ---------- layers ---------- */
  function lbox(y, t, sub, hot) { return '<rect x="10" y="' + y + '" width="330" height="54" rx="12" fill="var(--bg)" stroke="' + (hot ? "var(--go)" : "var(--line)") + '" stroke-width="1.5"/><text x="26" y="' + (y + 24) + '" font-size="16" font-weight="700" fill="var(--ink)">' + t + '</text><text x="26" y="' + (y + 43) + '" font-size="13" fill="var(--ink-3)">' + sub + '</text>'; }
  function sbox(y, t, sub) { return '<rect x="380" y="' + y + '" width="210" height="54" rx="12" fill="var(--bg)" stroke="var(--line)" stroke-dasharray="5 4"/><text x="396" y="' + (y + 24) + '" font-size="15" font-weight="700" fill="var(--ink)">' + t + '</text><text x="396" y="' + (y + 43) + '" font-size="12.5" fill="var(--ink-3)">' + sub + '</text>'; }
  function arr(y) { return '<line x1="175" y1="' + y + '" x2="175" y2="' + (y + 20) + '" stroke="var(--ink-3)" stroke-width="1.6" marker-end="url(#la)"/>'; }
  $("layers").innerHTML = '<svg viewBox="0 0 600 380" role="img" aria-label="Compose screens, ViewModels, WorkoutRepository, DAOs and Room, with DataStore and the Gson backup manager beside them"><defs><marker id="la" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto"><path d="M0 0L10 5L0 10z" fill="var(--ink-3)"/></marker></defs>' +
    lbox(4, "Compose screens", "Home, Workout, History, Stats, Profile", true) + arr(60) +
    lbox(82, "ViewModels", "UI state as StateFlow", false) + arr(138) +
    lbox(160, "WorkoutRepository", "streaks, PRs, stats, backup data", false) + arr(216) +
    lbox(238, "DAOs", "ExerciseDao, SessionDao, SetDao", false) + arr(294) +
    lbox(316, "Room on SQLite", "three tables, cascading deletes", true) +
    sbox(82, "DataStore", "your name, first launch") + sbox(238, "BackupManager", "Gson JSON via file picker") +
    '<line x1="340" y1="109" x2="378" y2="109" stroke="var(--ink-3)" stroke-dasharray="4 4"/><line x1="340" y1="187" x2="378" y2="265" stroke="var(--ink-3)" stroke-dasharray="4 4"/></svg>';

  /* ---------- notes ---------- */
  var NOTES = [
    ["bad", "Bug", "The streak resets every morning", "The streak may start from yesterday, but the loop then expects a workout today first. Until you train today, it shows 0, as the dashboard above does."],
    ["pr", "Naming", "“Recent PR” is your heaviest set ever", "It returns the single heaviest weight across all exercises and all time. Leg press will usually win, and it is never about recent progress."],
    ["bad", "Backup", "A restore can stop halfway", "Sessions and sets are inserted with the default abort rule and no transaction. Restoring onto a phone that already has workouts hits a duplicate id: the error is caught and reported, but some rows are already written."],
    ["", "Heatmap", "Three shades, one used", "The calendar component has light, medium and strong levels, but every active day is given 0.6. Switch the dashboard calendar to see what the levels would add."],
    ["", "Design", "Daily streaks punish rest days", "Streaks count calendar days in a row, so anyone with a rest day never gets past it. A weekly target would suit lifting better."],
    ["", "Volume", "Bodyweight work counts as zero", "Pull ups, dips and planks are logged with weight 0, so they add sets but no volume. Plank is logged in reps, not seconds."],
    ["", "Release", "Still com.example", "The application id is com.example.workouttracker. Google Play refuses that prefix, so it needs a real id before publishing."],
    ["", "Docs", "63 exercises, not 59", "The README promises 59 pre-loaded exercises; the seeder ships 63."],
    ["", "Speed", "One query per session", "Total sets, the PR and the backup each loop over every session with its own query, though a single count query already exists."]
  ];
  $("notes-list").innerHTML = NOTES.map(function (n) { return '<div class="card"><span class="chip ' + n[0] + '">' + n[1] + '</span><h3>' + n[2] + '</h3><p>' + esc(n[3]) + '</p></div>'; }).join("");

  /* ---------- misc ---------- */
  $("reset").addEventListener("click", function () {
    if (!confirm("Forget the sessions you logged on this page?")) return;
    store("rv-site-log", null); store("rv-site-current", null);
    cur = { type: "Push", exercises: R.TEMPLATES[0].exercises.map(function (e) { return e.id; }), sets: [], active: R.TEMPLATES[0].exercises[0].id, start: null }; dial = {};
    renderFloor(); renderDash(); renderProgress();
  });
  document.addEventListener("click", function (e) {
    var b = e.target.closest(".copy"); if (!b) return;
    var t = b.previousElementSibling.textContent;
    (navigator.clipboard ? navigator.clipboard.writeText(t) : Promise.reject()).then(function () { b.textContent = "Copied"; }, function () { b.textContent = "Select it"; });
    setTimeout(function () { b.textContent = "Copy"; }, 1400);
  });

  renderFloor(); renderDash(); renderProgress(); renderLib();
})();
