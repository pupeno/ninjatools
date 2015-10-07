-- Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

ALTER TABLE tools
ADD COLUMN "created_at" timestamp(6) NULL,
ADD COLUMN "updated_at" timestamp(6) NULL;
--;;
ALTER TABLE features
ADD COLUMN "created_at" timestamp(6) NULL,
ADD COLUMN "updated_at" timestamp(6) NULL;
--;;
ALTER TABLE tools_features
ADD COLUMN "created_at" timestamp(6) NULL,
ADD COLUMN "updated_at" timestamp(6) NULL;
--;;
ALTER TABLE integrations
ADD COLUMN "created_at" timestamp(6) NULL,
ADD COLUMN "updated_at" timestamp(6) NULL;
--;;
