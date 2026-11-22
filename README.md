# ResumeLens

Explainable resume analysis and job matching engine built with Java and Spring Boot.

ResumeLens reads a resume (PDF or DOCX), extracts skills, compares them with a
job description, and produces a compatibility score with a clear explanation of
how it was calculated. This is a custom compatibility metric, not a real ATS score.

## How the score works

`Score = 0.7·S + 0.2·T + 0.1·C`

| Term | Meaning | Calculation |
|------|---------|-------------|
| S | Skill coverage | Required skills (75%) and preferred skills (25%); exact match = 1.0, related skill = 0.5 |
| T | Evidence | Share of job skills that appear in Projects or Experience, not just the skills list |
| C | Completeness | Checklist: email, Education, Skills, Projects, Experience |

## Features

- PDF and DOCX text extraction (Apache PDFBox, Apache POI)
- Skill dictionary with synonym normalization (ML = Machine Learning, ReactJS = React)
- Whole-term matching, so Java never matches inside JavaScript
- Partial credit for related skills (PostgreSQL for SQL)
- Resume section detection (Education, Skills, Projects, Experience)
- Rule-based improvement suggestions
- REST API with validation and JSON error responses
- Saved analysis history (Spring Data JPA, H2 database)

## Tech stack

Java 21, Spring Boot, Spring Data JPA, H2, Maven, JUnit 5, Apache PDFBox, Apache POI

## Run

1. Open the project in IntelliJ IDEA (JDK 21).
2. Run `ResumeLensApplication`.
3. Check it is up: `GET http://localhost:8080/api/health`

## API

| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/analyze` | Analyze a resume. Form-data: `resume` (PDF/DOCX file), `jobDescription` (text), `jobTitle` (optional text) |
| GET | `/api/analyses` | List saved analyses |
| GET | `/api/analyses/{id}` | Get one saved analysis |
| DELETE | `/api/analyses/{id}` | Delete one saved analysis |

## Known limitations

- Scanned (image-only) PDFs are not supported (no OCR)
- Skill detection is keyword-based, so a skill is found only if the dictionary knows it
- Section detection relies on common headings such as "Education" or "Projects"

## Roadmap

- [x] Core engine: extraction, matching, scoring, suggestions
- [x] REST API for resume upload and analysis
- [x] Analysis history (JPA, H2)
- [x] Authentication (JWT) and per-user history
- [ ] MySQL
- [ ] React frontend
- [ ] Docker and deployment