# Git Workflow Best Practices

This document outlines a modern, robust Git workflow using the **Feature Branch Workflow** with **Pull Requests**.

## Core Philosophy

- **`main` is sacred:** The `main` branch is the single source of truth. It should always be stable, deployable, and contain production-ready code.
- **No direct commits to `main`:** All changes are developed in separate branches.
- **Branches for isolation:** Each new feature, bugfix, or chore is developed in its own dedicated branch.

---

## The Feature Branch Workflow

### 1. Starting a New Task (Feature or Bugfix)

1.  **Ensure your `main` branch is up-to-date:**

    ```bash
    git checkout main
    git pull origin main
    ```

2.  **Create a new branch:**
    - Name it something descriptive (e.g., `feature/short-description` or `fix/bug-name`).
    ```bash
    git checkout -b feature/user-authentication
    ```

### 2. Developing on the Feature Branch

1.  **Do your work:** Make all your code changes on this new branch.

2.  **Commit your changes:**
    - Commit your work frequently with clear, logical messages.

    ```bash
    git add .
    git commit -m "feat: Add user login endpoint"
    ```

3.  **Push your branch to the remote repository:**
    ```bash
    git push -u origin feature/user-authentication
    ```

### 3. Keeping Your Branch Updated

- Periodically update your branch with the latest changes from `main`.
  ```bash
  git pull origin main
  ```

### 4. Opening a Pull Request (PR) / Merge Request (MR)

1.  **When your feature is complete**, go to your Git hosting platform (GitHub, GitLab, etc.).
2.  Open a **Pull Request** to merge your feature branch into `main`.
3.  Write a clear title and description for your changes.

### 5. Code Review

- Your teammates will review your code and provide feedback.
- Make more commits to your branch to address the feedback. The Pull Request will update automatically.

### 6. Merging the Pull Request

- Once the PR is approved and passes any automated checks, it can be merged into `main`.
- It's common practice to **squash and merge**, which combines all of your feature branch's commits into a single commit on `main`.

### 7. Cleaning Up

- After your branch is merged, delete it.

  ```bash
  # Delete local branch
  git branch -d feature/user-authentication

  # Delete remote branch
  git push origin --delete feature/user-authentication
  ```

---

## Best Practices

- **Good Commit Messages:** Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification.
  - **Format:** `<type>[optional scope]: <description>`
  - **Examples:**
    - `feat: Allow users to upload a profile picture`
    - `fix: Correctly handle invalid login credentials`
    - `docs: Update the README with setup instructions`
- **Atomic Commits:** Each commit should represent a single logical change.
- **Continuous Integration (CI):** Set up automated tools to run tests and check code style on every Pull Request.
