package com.jandadav.hazelcast;

import com.hazelcast.instance.impl.DefaultNodeContext;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.instance.impl.NodeExtension;
import com.hazelcast.instance.impl.NodeExtensionFactory;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public class CustomNodeContext extends DefaultNodeContext {

    public static final List<String> CUSTOM_EXTENSION_PRIORITY_LIST = unmodifiableList(asList(
            "com.jandadav.hazelcast.CustomNodeExtension",
            "com.hazelcast.instance.impl.EnterpriseNodeExtension",
            "com.hazelcast.instance.impl.DefaultNodeExtension"
    ));

    @Override
    public NodeExtension createNodeExtension(Node node) {
        return NodeExtensionFactory.create(node, CUSTOM_EXTENSION_PRIORITY_LIST);
    }
}
