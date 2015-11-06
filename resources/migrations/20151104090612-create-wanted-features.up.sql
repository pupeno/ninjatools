-- Copyright © 2015 Carousel Apps, Ltd. All rights reserved

CREATE TABLE wanted_features
(id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 user_id UUID REFERENCES users (id),
 feature_id UUID REFERENCES features (id),
 created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'),
 updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'),
 CONSTRAINT used_features_user_id_feature_id UNIQUE (user_id, feature_id));
