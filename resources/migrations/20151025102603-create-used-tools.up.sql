-- Copyright © 2015 Carousel Apps, Ltd. All rights reserved

CREATE TABLE used_tools
(id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 user_id UUID,
 tool_id UUID,
 created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'),
 updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'));
--;;
CREATE UNIQUE INDEX used_tools_user_id_tool_id ON used_tools (user_id, tool_id);


