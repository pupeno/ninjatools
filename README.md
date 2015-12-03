# ninjatools

## Databases

Use these commands to create the databases:

```sql
CREATE DATABASE "ninjatools_dev" WITH OWNER "your-username" ENCODING 'UTF8';
CREATE DATABASE "ninjatools_test" WITH OWNER "your-username" ENCODING 'UTF8';
```

## Updating

To update to a more current luminus template, in a different directory run:

```
lein new luminus ninjatools +site +cljs +auth +postgres +swagger
```

and then diff the two directories and copy what you want.

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run at the same time:

    lein run

    lein figwheel

Open http://ninjatools.lvh.me:3000/

## License

Copyright © 2015 Carousel Apps, Ltd. All rights reserved.
