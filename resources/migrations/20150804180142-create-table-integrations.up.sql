-- Copyright © 2015 Carousel Apps, Ltd. All rights reserved

CREATE TABLE integrations
(id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 tool_a_id UUID NOT NULL REFERENCES tools ON UPDATE CASCADE ON DELETE CASCADE,
 tool_b_id UUID NOT NULL REFERENCES tools ON UPDATE CASCADE ON DELETE CASCADE,
 comment TEXT DEFAULT '');
--;;
CREATE UNIQUE INDEX integrations_id ON integrations (id);
--;;
CREATE UNIQUE INDEX integrations_tool_a_id_tool_b_id ON integrations (tool_a_id, tool_b_id);