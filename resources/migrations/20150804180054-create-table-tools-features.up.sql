-- Copyright © 2015 Carousel Apps, Ltd. All rights reserved

CREATE TABLE tools_features
(id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 tool_id UUID NOT NULL REFERENCES tools ON UPDATE CASCADE ON DELETE CASCADE,
 feature_id UUID NOT NULL REFERENCES features ON UPDATE CASCADE ON DELETE CASCADE,
 description TEXT);
--;;
CREATE UNIQUE INDEX tools_features_id ON tools_features (id);
--;;
CREATE UNIQUE INDEX tools_features_tool_id_feature_id ON tools_features (tool_id, feature_id);