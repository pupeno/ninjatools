-- Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

ALTER TABLE tools
DROP COLUMN "created_at",
DROP COLUMN "updated_at";
--;;
ALTER TABLE features
DROP COLUMN "created_at",
DROP COLUMN "updated_at";
--;;
ALTER TABLE tools_features
DROP COLUMN "created_at",
DROP COLUMN "updated_at";
--;;
ALTER TABLE integrations
DROP COLUMN "created_at",
DROP COLUMN "updated_at";
--;;
