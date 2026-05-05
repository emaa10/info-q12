# Wochenbericht – 2026-W19 – Emanuel Berger

_Automatisch erstellt am 2026-05-05 mit GitHub Copilot (KI)_

## Zusammenfassung

# Wochenbericht für das Schulprojekt von Emanuel Berger

## Zusammenfassung der Änderungen (Woche vom 28. April bis 5. Mai 2026)

- **Commit 5324e069 (2026-05-05)**: 
  - **Merge von Pull Request #4**: Einführung eines neuen GitHub Actions Workflows zur automatischen Generierung eines wöchentlichen Changelogs.
  - **Neue Datei**: `.github/workflows/weekly-changelog.yml` mit 251 Zeilen Code.
    - Der Workflow wird jeden Montag um 08:00 UTC ausgeführt.
    - Er ermöglicht auch eine manuelle Auslösung zur Testzwecken.
    - Der Workflow beinhaltet folgende Schritte:
      - Checkout des Repositories.
      - Einrichtung von Python (Version 3.12).
      - Installation der Python-Abhängigkeit `openai`.
      - Generierung des wöchentlichen Changelogs durch ein Python-Skript, das:
        - Commits der letzten 7 Tage abruft.
        - Für jeden Commit eine Zusammenfassung erstellt, die die Änderungen und die geänderten Dateien umfasst.
        - Die Ergebnisse in Markdown-Dateien pro Benutzer im Verzeichnis `changelogs/<Jahr-Woche>/` speichert.
      - Bei fehlenden Commits wird eine Platzhalterdatei erstellt.

- **Commit 47fde36d (2026-04-28)**: 
  - **Update der README.md**:
    - Hinzufügen von Kontaktdaten für Herrn Stark:
      - **E-Mail**: starkrobert@gym-indersdorf.de
      - **Betreff**: Berger_1.Woche z.B.
  
- **Commit 384c2a9a (2026-04-28)**: 
  - **Update der README.md**:
    - Ergänzung eines Badges für das Projekt-Board, das auf die GitHub-Projektseite verweist.
    - Eine Zeile wurde entfernt, um die Formatierung zu verbessern.

## Fazit
In dieser Woche wurden wesentliche Fortschritte bei der Automatisierung der Dokumentation des Projekts erzielt, insbesondere durch die Implementierung eines Workflows zur Generierung eines wöchentlichen Changelogs. Zudem wurden die Kontaktdaten in der README-Datei aktualisiert, um die Kommunikation zu erleichtern.

---

## Commits im Detail

### `5324e069` – 2026-05-05: Merge pull request #4 from emaa10/copilot/write-github-actions-for-email

**Geänderte Dateien:**

```
.github/workflows/weekly-changelog.yml | 251 +++++++++++++++++++++++++++++++++
 1 file changed, 251 insertions(+)
```

<details>
<summary>Diff anzeigen</summary>

```diff
diff --git a/.github/workflows/weekly-changelog.yml b/.github/workflows/weekly-changelog.yml
new file mode 100644
index 0000000..44c4226
--- /dev/null
+++ b/.github/workflows/weekly-changelog.yml
@@ -0,0 +1,251 @@
+name: Weekly Changelog
+
+on:
+  schedule:
+    # Runs every Monday at 08:00 UTC
+    - cron: '0 8 * * 1'
+  workflow_dispatch: # allow manual trigger for testing
+
+permissions:
+  contents: write
+  models: read  # required for GitHub Models (Copilot) API
+
+jobs:
+  generate-changelog:
+    runs-on: ubuntu-latest
+    steps:
+      - name: Checkout repository
+        uses: actions/checkout@v4
+        with:
+          fetch-depth: 0  # full history needed to get weekly commits
+          ref: main
+
+      - name: Set up Python
+        uses: actions/setup-python@v5
+        with:
+          python-version: '3.12'
+
+      - name: Install Python dependencies
+        run: pip install openai
+
+      - name: Generate weekly changelog
+        env:
+          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
+        run: |
+          python - <<'PYEOF'
+          import subprocess
+          import os
+          import re
+          from datetime import datetime, timezone, timedelta
+          from collections import defaultdict
+          from openai import OpenAI
+
+          # ── GitHub Models (Copilot) client ────────────────────────────────
+          client = OpenAI(
+              base_url="https://models.inference.ai.azure.com",
+              api_key=os.environ["GITHUB_TOKEN"],
+          )
+
+          MAX_DIFF_LINES = 200  # max lines per diff kept in the changelog
+
+          def ai_summary(author, commits):
+              """Ask GitHub Models to write a German summary for one user's week."""
+              commit_details = []
+              for c in commits:
+                  block = f"Commit {c['sha']} ({c['date']}): {c['subject']}\n"
+                  if c["stat"]:
+                      block += f"Geänderte Dateien:\n{c['stat']}\n"
+                  if c["diff"]:
+                      block += f"Diff (gekürzt):\n{c['diff']}\n"
+                  commit_details.append(block)
+
+              prompt = (
+                  f"Du bist ein hilfreicher Assistent, der Wochenberichte für ein Schulprojekt erstellt.\n"
+                  f"Fasse die folgenden Git-Commits von '{author}' auf Deutsch zusammen. "
+                  f"Beschreibe präzise, was diese Woche inhaltlich geändert wurde – "
+                  f"nicht nur die Commit-Nachrichten, sondern auch die tatsächlichen Code-Änderungen. "
+                  f"Schreibe einen übersichtlichen Markdown-Bericht mit Aufzählungspunkten.\n\n"
+                  + "\n---\n".join(commit_details)
+              )
+
+              response = client.chat.completions.create(
+                  model="gpt-4o-mini",
+                  messages=[{"role": "user", "content": prompt}],
+                  temperature=0.3,
+                  max_tokens=2048,
+              )
+              return response.choices[0].message.content.strip()
+
+          # ── Date window ───────────────────────────────────────────────────
+          now        = datetime.now(timezone.utc)
+          week_start = now - timedelta(days=7)
+          iso_since  = week_start.strftime("%Y-%m-%dT%H:%M:%SZ")
+          year_week  = now.strftime("%Y-W%V")   # e.g. 2025-W18
+          date_label = now.strftime("%Y-%m-%d")
+
+          # ── Collect commits from the last 7 days ──────────────────────────
+          log_cmd = [
+              "git", "log",
+              f"--after={iso_since}",
+              "--format=%H\x1f%an\x1f%ae\x1f%ad\x1f%s",
+              "--date=short",
+          ]
+          result = subprocess.run(log_cmd, capture_output=True, text=True, check=True)
+          raw_lines = [l for l in result.stdout.strip().splitlines() if l]
+
+          # author -> list of commit detail dicts
+          by_author = defaultdict(list)
+
+          for line in raw_lines:
+              parts = line.split("\x1f", 4)
+              if len(parts) < 5:
+                  continue
+              sha, author, email, date, subject = parts
+
+              # diff stat — sha^ may not exist for root commits, so check returncode
+              proc_stat = subprocess.run(
+                  ["git", "diff", "--stat", f"{sha}^", sha],
+                  capture_output=True, text=True
+              )
+              stat = proc_stat.stdout.strip() if proc_stat.returncode == 0 else ""
+              # handle root commits (no parent)
+              if not stat:
+                  stat = subprocess.run(
+                      ["git", "show", "--stat", "--format=", sha],
+                      capture_output=True, text=True, check=True
+                  ).stdout.strip()
+
+              # actual diff — same fallback for root commits
+              proc_diff = subprocess.run(
+                  ["git", "diff", f"{sha}^", sha],
+                  capture_output=True, text=True
+              )
+              diff_full = proc_diff.stdout if proc_diff.returncode == 0 else ""
+              if not diff_full:
+                  diff_full = subprocess.run(
+                      ["git", "show", "--format=", sha],
+                      capture_output=True, text=True, check=True
+                  ).stdout
+              diff_lines = diff_full.splitlines()
+              if len(diff_lines) > MAX_DIFF_LINES:
+                  diff_lines = diff_lines[:MAX_DIFF_LINES] + ["... (Diff gekürzt)"]
+              diff_body = "\n".join(diff_lines)
+
+              by_author[author].append({
+                  "sha":     sha[:8],
+                  "date":    date,
+                  "subject": subject,
+                  "stat":    stat,
+                  "diff":    diff_body,
+              })
+
+          # ── Write one file per user: changelogs/<week>/<username>.md ─────
+          week_dir = os.path.join("changelogs", year_week)
+          os.makedirs(week_dir, exist_ok=True)
+
+          if not by_author:
+              placeholder = os.path.join(week_dir, "keine-commits.md")
+              with open(placeholder, "w", encoding="utf-8") as f:
+                  f.write(f"# Changelog – Woche {year_week}\n\n_Keine Commits diese Woche._\n")
+              print(f"Geschrieben: {placeholder}")
+          else:
+              for author, commits in by_author.items():
+                  safe_name = re.sub(r"[^\w\-]", "_", author).strip("_").lower()
+                  user_file = os.path.join(week_dir, f"{safe_name}.md")
+
+                  print(f"KI-Zusammenfassung wird erstellt für: {author} …")
+                  summary = ai_summary(author, commits)
+
+                  file_lines = [
+                      f"# Wochenbericht – {year_week} – {author}",
+                      "",
+                      f"_Automatisch erstellt am {date_label} mit GitHub Copilot (KI)_",
+                      "",
+                      "## Zusammenfassung",
+                      "",
+                      summary,
+                      "",
+                      "---",
+                      "",
+                      "## Commits im Detail",
+                      "",
+                  ]
+                  for c in commits:
+                      file_lines.append(f"### `{c['sha']}` – {c['date']}: {c['subject']}")
+                      file_lines.append("")
+                      if c["stat"]:
+                          file_lines.append("**Geänderte Dateien:**")
+                          file_lines.append("")
+                          file_lines.append("```")
+                          file_lines.append(c["stat"])
+                          file_lines.append("```")
+                          file_lines.append("")
+                      if c["diff"]:
+                          file_lines.append("<details>")
+                          file_lines.append("<summary>Diff anzeigen</summary>")
+                          file_lines.append("")
+                          file_lines.append("```diff")
+                          file_lines.append(c["diff"])
+                          file_lines.append("```")
+                          file_lines.append("")
+                          file_lines.append("</details>")
+                          file_lines.append("")
+
+                  with open(user_file, "w", encoding="utf-8") as f:
... (Diff gekürzt)
```

</details>

### `47fde36d` – 2026-04-28: Update README.md

**Geänderte Dateien:**

```
README.md | 4 ++++
 1 file changed, 4 insertions(+)
```

<details>
<summary>Diff anzeigen</summary>

```diff
diff --git a/README.md b/README.md
index 06cae69..d5ef16d 100644
--- a/README.md
+++ b/README.md
@@ -3,3 +3,7 @@ Repository for the project in the IT class in grade 12
 
 # Documentation
 [![Project Board](https://img.shields.io/badge/Project-Board-blue?logo=github)](https://github.com/users/emaa10/projects/2)
+
+# Email an Herr Stark
+**an** starkrobert@gym-indersdorf.de 
+**Betreff**: Berger_1.Woche z.b.
```

</details>

### `384c2a9a` – 2026-04-28: Update README.md

**Geänderte Dateien:**

```
README.md | 3 ++-
 1 file changed, 2 insertions(+), 1 deletion(-)
```

<details>
<summary>Diff anzeigen</summary>

```diff
diff --git a/README.md b/README.md
index 74fa449..06cae69 100644
--- a/README.md
+++ b/README.md
@@ -1,4 +1,5 @@
 # info-q12
 Repository for the project in the IT class in grade 12
 
-# Documentation
\ No newline at end of file
+# Documentation
+[![Project Board](https://img.shields.io/badge/Project-Board-blue?logo=github)](https://github.com/users/emaa10/projects/2)
```

</details>

