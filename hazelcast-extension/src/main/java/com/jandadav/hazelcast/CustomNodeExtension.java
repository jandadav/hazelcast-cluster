package com.jandadav.hazelcast;

import com.hazelcast.instance.impl.DefaultNodeExtension;
import com.hazelcast.instance.impl.Node;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomNodeExtension extends DefaultNodeExtension {

    public CustomNodeExtension(Node node) {
        super(node);
        log.info("+++++++ ALL YOUR BASES ARE BELONG TO US +++++++");
    }

}
