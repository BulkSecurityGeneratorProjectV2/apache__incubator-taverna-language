package uk.org.taverna.scufl2.api.iterationstrategy;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import uk.org.taverna.scufl2.api.annotation.Annotation;
import uk.org.taverna.scufl2.api.common.AbstractCloneable;
import uk.org.taverna.scufl2.api.common.Child;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.common.URITools;
import uk.org.taverna.scufl2.api.common.Visitor;
import uk.org.taverna.scufl2.api.common.WorkflowBean;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;

public class PortNode extends AbstractCloneable implements IterationStrategyNode {
	private InputProcessorPort inputProcessorPort;

	private IterationStrategyParent parent;
	private Integer desiredDepth;

	public PortNode() {
	}

	public PortNode(IterationStrategyParent parent,
			InputProcessorPort inputProcessorPort) {
		setParent(parent);
		setInputProcessorPort(inputProcessorPort);
	}

	@Override
	public boolean accept(Visitor visitor) {
		return visitor.visit(this);
	}

	public Integer getDesiredDepth() {
		return desiredDepth;
	}

	public InputProcessorPort getInputProcessorPort() {
		return inputProcessorPort;
	}

	@Override
	public IterationStrategyParent getParent() {
		return parent;
	}

	public void setDesiredDepth(Integer desiredDepth) {
		this.desiredDepth = desiredDepth;
	}

	public void setInputProcessorPort(InputProcessorPort inputProcessorPort) {
		this.inputProcessorPort = inputProcessorPort;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((desiredDepth == null) ? 0 : desiredDepth.hashCode());
        result = prime
                * result
                + ((inputProcessorPort == null) ? 0 : inputProcessorPort
                        .hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof PortNode))
            return false;
        PortNode other = (PortNode) obj;
        if (desiredDepth == null) {
            if (other.desiredDepth != null)
                return false;
        } else if (!desiredDepth.equals(other.desiredDepth))
            return false;
        if (inputProcessorPort == null) {
            if (other.inputProcessorPort != null)
                return false;
        } else if (!inputProcessorPort.equals(other.inputProcessorPort))
            return false;
        return true;
    }

	private static boolean saneParent(IterationStrategyParent node) {
		return (node == null) || (node instanceof DotProduct)
				|| (node instanceof CrossProduct);
	}

    @Override
	public void setParent(IterationStrategyParent newParent) {
		if (parent == newParent)
			return;
		if (!saneParent(newParent))
			throw new IllegalArgumentException(
					"PortNode parent must be a DotProduct or CrossProduct: "
							+ parent);

		if (parent != null) {
			// Remove from old parent
			if (!saneParent(parent))
				throw new IllegalArgumentException(
						"Old PortNode parent must be a DotProduct or CrossProduct: "
								+ parent);
			@SuppressWarnings("unchecked")
			List<IterationStrategyNode> parentList = (List<IterationStrategyNode>) parent;
			parentList.remove(this);
		}

		parent = newParent;
		@SuppressWarnings("unchecked")
		List<IterationStrategyNode> parentList = (List<IterationStrategyNode>) parent;
		if (!parentList.contains(this))
			parentList.add(this);
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()  + " for " + getInputProcessorPort();
	}
	
	@Override
	protected void cloneInto(WorkflowBean clone, Cloning cloning) {
		PortNode cloneNode = (PortNode)clone;
		cloneNode.setDesiredDepth(getDesiredDepth());
		cloneNode.setInputProcessorPort(cloning.cloneOrOriginal(getInputProcessorPort()));		
	}

	// Derived operations

	/**
	 * Get all the annotations that pertain to this port node.
	 * 
	 * @return The collection of annotations.
	 * @see Scufl2Tools#annotationsFor(Child)
	 */
	public Collection<Annotation> getAnnotations() {
		return getTools().annotationsFor(this);
	}

	/**
	 * Get the URI of this port node.
	 * 
	 * @return The absolute URI.
	 * @see URITools#uriForBean(WorkflowBean)
	 */
	public URI getURI() {
		return getUriTools().uriForBean(this);
	}

	/**
	 * Get the URI of this port node relative to another workflow element.
	 * 
	 * @return The relative URI.
	 * @see URITools#relativeUriForBean(WorkflowBean,WorflowBean)
	 */
	public URI getRelativeURI(WorkflowBean relativeTo) {
		return getUriTools().relativeUriForBean(this, relativeTo);
	}
}
