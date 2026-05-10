# Wochenbericht – 2026-W19 – Emanuel Berger

_Automatisch erstellt am 2026-05-10 mit GitHub Copilot (KI)_

## E-Mail-Zusammenfassung

Betreff: Wochenzusammenfassung

Hallo Emanuel,

hier sind die wichtigsten Punkte dieser Woche:

- Aktualisierung der Datei weekly-changelog.yml
- Zusammenführung von PR #12 zur Aktualisierung der GitHub-Aktion
- Aktualisierung der README.md
- Zusammenführung von PR #11: Inhaltsverzeichnis zur README hinzugefügt
- Zusammenführung von PR #5 und PR #4: Zusammenfassung und GitHub-Aktionen für E-Mail erstellt

Viele Grüße,  
[Dein Name]

---

## Zusammenfassung

# Wochenbericht – Zusammenfassung der Änderungen von Emanuel Berger

In dieser Woche wurden mehrere Änderungen am Projekt vorgenommen, die sich hauptsächlich auf die Automatisierung von Arbeitsabläufen und die Dokumentation konzentrierten. Hier sind die wichtigsten Punkte:

## Änderungen an den GitHub Actions

- **Update der Zeitplanung für den wöchentlichen Changelog**:
  - Der Zeitplan für die Ausführung des wöchentlichen Changelogs wurde von **jeden Montag um 08:00 UTC** auf **jeden Sonntag um 10:00 UTC** geändert. Dies wurde in der Datei `.github/workflows/weekly-changelog.yml` aktualisiert.

- **Erstellung einer neuen GitHub Action für die Aktualisierung des Inhaltsverzeichnisses**:
  - Eine neue Datei `.github/workflows/toc.yml` wurde hinzugefügt, die eine Action definiert, die das Inhaltsverzeichnis der `README.md` automatisch aktualisiert, wenn Änderungen an dieser Datei vorgenommen werden. Diese Action verwendet ein Python-Skript, um die Überschriften zu sammeln und ein Inhaltsverzeichnis zu generieren.

- **Erweiterung der wöchentlichen Changelog-Action**:
  - Die Action zur Erstellung des wöchentlichen Changelogs wurde erheblich erweitert. Es wurden Funktionen hinzugefügt, die eine kurze E-Mail-Zusammenfassung der Commits generieren. Diese Zusammenfassung wird in den wöchentlichen Bericht eingefügt und enthält prägnante Stichpunkte, die die Änderungen der Woche zusammenfassen.

## Änderungen an der Dokumentation

- **Änderung des Repository-Namens in der README**:
  - Der Titel des Repositories in der `README.md` wurde von `# info-q12` auf `# Informatik Projekt` geändert, um den Inhalt des Projekts klarer zu kennzeichnen.

- **Aktualisierung des Inhaltsverzeichnisses**:
  - Mit der neuen Action wird das Inhaltsverzeichnis der `README.md` automatisch aktualisiert, was die Navigation im Dokument erleichtert.

## Zusammenfassung der Commits

- **Commit c403de20**: Aktualisierung der Zeitplanung für den wöchentlichen Changelog.
- **Commit 220967f1**: Zusammenführung eines Pull Requests, der die Zeitplanung für den wöchentlichen Changelog aktualisiert hat.
- **Commit 71aad99d**: Änderung des Repository-Namens in der README.
- **Commit 8b595337**: Hinzufügen einer neuen Action zur automatischen Aktualisierung des Inhaltsverzeichnisses in der README.
- **Commit 879f6d37**: Erweiterung der wöchentlichen Changelog-Action um eine E-Mail-Zusammenfassung der Commits.
- **Commit 5324e069**: Einführung der wöchentlichen Changelog-Action mit umfassenden Funktionen zur Generierung von Berichten.

Diese Änderungen verbessern die Automatisierung und Dokumentation des Projekts erheblich und tragen zur Effizienz bei der Verwaltung von Änderungen und der Kommunikation innerhalb des Teams bei.

---

## Commits im Detail

### `c403de20` – 2026-05-06: Update weekly-changelog.yml

**Geänderte Dateien:**

```
.github/workflows/weekly-changelog.yml | 2 +-
 1 file changed, 1 insertion(+), 1 deletion(-)
```

<details>
<summary>Diff anzeigen</summary>

```diff
diff --git a/.github/workflows/weekly-changelog.yml b/.github/workflows/weekly-changelog.yml
index 5810c00..856716e 100644
--- a/.github/workflows/weekly-changelog.yml
+++ b/.github/workflows/weekly-changelog.yml
@@ -2,7 +2,7 @@ name: Weekly Changelog
 
 on:
   schedule:
-    # Runs every Monday at 08:00 UTC
+    # Runs every Sunday at 10:00 UTC
     - cron: '0 10 * * 0'
   workflow_dispatch: # allow manual trigger for testing
 
```

</details>

### `220967f1` – 2026-05-06: Merge pull request #12 from emaa10/copilot/update-github-action-time

**Geänderte Dateien:**

```
.github/workflows/weekly-changelog.yml | 2 +-
 1 file changed, 1 insertion(+), 1 deletion(-)
```

<details>
<summary>Diff anzeigen</summary>

```diff
diff --git a/.github/workflows/weekly-changelog.yml b/.github/workflows/weekly-changelog.yml
index a41eafb..5810c00 100644
--- a/.github/workflows/weekly-changelog.yml
+++ b/.github/workflows/weekly-changelog.yml
@@ -3,7 +3,7 @@ name: Weekly Changelog
 on:
   schedule:
     # Runs every Monday at 08:00 UTC
-    - cron: '0 8 * * 1'
+    - cron: '0 10 * * 0'
   workflow_dispatch: # allow manual trigger for testing
 
 permissions:
```

</details>

### `71aad99d` – 2026-05-05: Update README.md

**Geänderte Dateien:**

```
README.md | 2 +-
 1 file changed, 1 insertion(+), 1 deletion(-)
```

<details>
<summary>Diff anzeigen</summary>

```diff
diff --git a/README.md b/README.md
index dd51135..0b7e4b8 100644
--- a/README.md
+++ b/README.md
@@ -1,4 +1,4 @@
-# info-q12
+# Informatik Projekt
 Repository for the project in the IT class in grade 12
 
 # Documentation
```

</details>

### `8b595337` – 2026-05-05: Merge pull request #11 from emaa10/copilot/add-table-of-contents-to-readme

**Geänderte Dateien:**

```
.github/workflows/toc.yml | 89 +++++++++++++++++++++++++++++++++++++++++++++++
 1 file changed, 89 insertions(+)
```

<details>
<summary>Diff anzeigen</summary>

```diff
diff --git a/.github/workflows/toc.yml b/.github/workflows/toc.yml
new file mode 100644
index 0000000..38352f1
--- /dev/null
+++ b/.github/workflows/toc.yml
@@ -0,0 +1,89 @@
+name: Update README Table of Contents
+
+on:
+  push:
+    branches:
+      - main
+    paths:
+      - 'README.md'
+
+permissions:
+  contents: write
+
+jobs:
+  toc:
+    runs-on: ubuntu-latest
+    steps:
+      - name: Checkout repository
+        uses: actions/checkout@v4
+        with:
+          ref: main
+
+      - name: Generate and insert Table of Contents
+        run: |
+          python3 - <<'PYEOF'
+          import re
+
+          README = "README.md"
+          TOC_START = "<!-- TOC -->"
+          TOC_END   = "<!-- /TOC -->"
+
+          with open(README, "r", encoding="utf-8") as f:
+              content = f.read()
+
+          # Remove existing TOC block (including surrounding blank lines) if present
+          content = re.sub(
+              r"\n?" + re.escape(TOC_START) + r".*?" + re.escape(TOC_END) + r"\n?",
+              "",
+              content,
+              flags=re.DOTALL,
+          ).lstrip("\n")
+
+          # Collect all headings
+          heading_re = re.compile(r"^(#{1,6})\s+(.+)", re.MULTILINE)
+          headings = heading_re.findall(content)
+
+          def slugify(text):
+              """GitHub-compatible anchor slugs."""
+              text = text.lower()
+              text = re.sub(r"[^\w\s-]", "", text)   # strip special chars except - and _
+              text = re.sub(r"[\s]+", "-", text.strip())
+              return text
+
+          # Count duplicate slugs so we can append -1, -2 … like GitHub does
+          slug_counts = {}
+          toc_lines = []
+          for hashes, title in headings:
+              level  = len(hashes)
+              indent = "  " * (level - 1)
+              slug   = slugify(title)
+              count  = slug_counts.get(slug, 0)
+              slug_counts[slug] = count + 1
+              anchor = slug if count == 0 else f"{slug}-{count}"
+              # Strip any inline markdown from the visible label
+              label = re.sub(r"[*_`~\[\]]", "", title).strip()
+              toc_lines.append(f"{indent}- [{label}](#{anchor})")
+
+          toc_block = TOC_START + "\n" + "\n".join(toc_lines) + "\n" + TOC_END
+
+          new_content = toc_block + "\n\n" + content
+
+          with open(README, "w", encoding="utf-8") as f:
+              f.write(new_content)
+
+          print("TOC updated successfully.")
+          PYEOF
+
+      - name: Commit and push updated README
+        run: |
+          git config user.name  "github-actions[bot]"
+          git config user.email "github-actions[bot]@users.noreply.github.com"
+
+          git add README.md
+
+          if git diff --cached --quiet; then
+            echo "TOC unchanged – nothing to commit."
+          else
+            git commit -m "docs: update README table of contents [skip ci]"
+            git push origin main
+          fi
```

</details>

### `879f6d37` – 2026-05-05: Merge pull request #5 from emaa10/copilot/add-summary-to-email

**Geänderte Dateien:**

```
.github/workflows/weekly-changelog.yml | 28 ++++++++++++++++++++++++++++
 1 file changed, 28 insertions(+)
```

<details>
<summary>Diff anzeigen</summary>

```diff
diff --git a/.github/workflows/weekly-changelog.yml b/.github/workflows/weekly-changelog.yml
index 44c4226..a41eafb 100644
--- a/.github/workflows/weekly-changelog.yml
+++ b/.github/workflows/weekly-changelog.yml
@@ -76,6 +76,25 @@ jobs:
               )
               return response.choices[0].message.content.strip()
 
+          def ai_email_summary(author, commits):
+              """Ask GitHub Models for 3-5 short German bullet points (email style)."""
+              subjects = "\n".join(
+                  f"- [{c['date']}] {c['subject']}" for c in commits
+              )
+              prompt = (
+                  f"Erstelle eine kurze E-Mail-Zusammenfassung auf Deutsch für '{author}'.\n"
+                  f"Nutze maximal 5 prägnante Stichpunkte, die beschreiben, was diese Woche "
+                  f"gemacht wurde. Keine langen Erklärungen – nur kurze Stichpunkte.\n\n"
+                  f"Commits:\n{subjects}"
+              )
+              response = client.chat.completions.create(
+                  model="gpt-4o-mini",
+                  messages=[{"role": "user", "content": prompt}],
+                  temperature=0.3,
+                  max_tokens=512,
+              )
+              return response.choices[0].message.content.strip()
+
           # ── Date window ───────────────────────────────────────────────────
           now        = datetime.now(timezone.utc)
           week_start = now - timedelta(days=7)
@@ -156,11 +175,20 @@ jobs:
                   print(f"KI-Zusammenfassung wird erstellt für: {author} …")
                   summary = ai_summary(author, commits)
 
+                  print(f"E-Mail-Zusammenfassung wird erstellt für: {author} …")
+                  email_summary = ai_email_summary(author, commits)
+
                   file_lines = [
                       f"# Wochenbericht – {year_week} – {author}",
                       "",
                       f"_Automatisch erstellt am {date_label} mit GitHub Copilot (KI)_",
                       "",
+                      "## E-Mail-Zusammenfassung",
+                      "",
+                      email_summary,
+                      "",
+                      "---",
+                      "",
                       "## Zusammenfassung",
                       "",
                       summary,
```

</details>

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

