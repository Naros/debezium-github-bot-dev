# Debezium GitHub Bot

## Powered by

This project uses Quarkus, the Supersonic Stubatomic Java Framework.

If you want to learn more about Quarkus, visit its website: https://quarkus.io/.

Specifically, most of the GitHub-related features in this bot are powered by
[the `quarkus-github-app` extension](https://github.com/quarkiverse/quarkus-github-app).

## Features

This bot checks various contribution rules on pull requests submitted to Debezium projects on GitHub,
and notifies the pull request authors of any change they need to work on.

This includes:

* Basic formatting of the pull request: at least two words in the title, ...
* Proper referencing of related JIRA tickets: the ticket key must be mentioned in the PR description.
* Lists associated JIRA ticket URLs in the comments based on the keys found in title and commits.
* Etc.

## Configuration

### Enabling the bot in a new repository

You will need admin rights in the Debezium organization.

Go to [the installed application settings](https://github.com/organizations/debezium/settings/installations/)
and add your repository under "Repository access".

## Contributing

Always test your changes locally before pushing them.

You can run the bot locally by:

1. Registering a test instance of the GitHub application on a fake, "playground" repository
   [as explained here](https://quarkiverse.github.io/quarkiverse-docs/quarkus-github-app/dev/register-github-app.html).
2. Adding an `.env` file at the root of the repository,
   [as explained here](https://quarkiverse.github.io/quarkiverse-docs/quarkus-github-app/dev/create-github-app.html#_initialize_the_configuration).
3. Running `./mvnw quarkus:dev`.

