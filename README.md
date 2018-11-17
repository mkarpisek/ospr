# ospr

[![Build Status](https://travis-ci.org/mkarpisek/orb.svg?branch=master)](https://travis-ci.org/mkarpisek/ospr)
[![codecov](https://codecov.io/gh/mkarpisek/ospr/branch/master/graph/badge.svg)](https://codecov.io/gh/mkarpisek/ospr)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/mkarpisek/ospr.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mkarpisek/ospr/alerts/)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/mkarpisek/ospr.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mkarpisek/ospr/context:java)

Office 365 Sharepoint File Reporting Tool

## Getting Started
### Prerequisites

Java 8

### Build

Build code and package as one big uberjar:
```
mvn clean package
```

### Run
```
Usage: java -jar ospr.jar [-hV] -p=PASSWORD -u=USERNAME URL
Office 365 Sharepoint File Reporting Tool
      URL                   sharepoint site/library/folder/subfolder url, in format
                              'https://yourdomain.sharepoint.
                              com/sites/siteName/libraryName/folderName', if URL is
                              for site only uses 'Shared Documents' as default
                              libraryName
  -h, --help                Show this help message and exit.
  -p, --password=PASSWORD   for sharepoint account to use
  -u, --user=USERNAME       sharepoint account username in format
                              <userName>@<yourdomain>.onmicrosoft.com
  -V, --version             Print version information and exit.
```
## License

This project is licensed under the EPL 2.0 License - see the [LICENSE.md](LICENSE.md) file for details
