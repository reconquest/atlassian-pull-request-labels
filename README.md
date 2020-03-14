# Atlassian Bitbucket Pull Request Labels by Reconquest

[Documentation](https://labels.reconquest.io/)

![screenshot](https://labels.reconquest.io/images/applied-labels-filter.png)

A Bitbucket Server app that enables you to add labels to your pull requests.

If you're looking for the ready-to-use app, it is available on the [Atlassian Marketplace](https://marketplace.atlassian.com/apps/1219710/pull-request-labels-by-reconquest?hosting=server&tab=overview).

## Support

Please [open an issue](https://github.com/reconquest/bitbucket-labels/issues/new) for support.

You can always join our Slack community and drop us a line there: [slack.reconquest.io](https://slack.reconquest.io/)

## Running Locally

After setup, Bitbucket instance will be available at https://bitbucket.local/.

Requirements:

* Docker
* [Task](https://taskfile.dev)
* [stacket](https://github.com/kovetskiy/stacket/)
* [Atlassian Plugin SDK](https://aur.archlinux.org/packages/atlassian-plugin-sdk-latest/)

### Run Dev Bitbucket Instance

```
task version=<target-bitbucket-version> docker:run
task nginx:run
```

### Add `bitbucket.local` to `/etc/hosts`

```
127.0.0.1 bitbucket.local
```

### Compile and Install Plugin

```
task version=<target-bitbucket-version> js=batch.loader.js atlas:install
```

## License

This project is licensed under the GNU General Public License version 2.
