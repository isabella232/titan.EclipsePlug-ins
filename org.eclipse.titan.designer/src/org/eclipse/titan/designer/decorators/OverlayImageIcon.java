/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.decorators;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * @author Kristof Szabados
 * */
public class OverlayImageIcon extends CompositeImageDescriptor {

	private final Image baseImage;
	private final Point sizeOfImage;

	private final Image decorator;
	private final Position position;

	public enum Position {
		TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT
	}

	public OverlayImageIcon(final Image baseImage, final Image decorator, final Position position) {
		this.baseImage = baseImage;
		this.decorator = decorator;
		this.position = position;
		sizeOfImage = new Point(baseImage.getBounds().width, baseImage.getBounds().height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage
	 * (int, int)
	 */
	@Override
	protected void drawCompositeImage(final int width, final int height) {
		drawImage(baseImage.getImageData(), 0, 0);

		final ImageData imageData = decorator.getImageData();
		switch (position) {
		case TOP_LEFT:
			drawImage(imageData, 0, 0);
			break;
		case TOP_RIGHT:
			drawImage(imageData, sizeOfImage.x - imageData.width, 0);
			break;
		case BOTTOM_LEFT:
			drawImage(imageData, 0, sizeOfImage.y - imageData.height);
			break;
		case BOTTOM_RIGHT:
			drawImage(imageData, sizeOfImage.x - imageData.width, sizeOfImage.y - imageData.height);
			break;
		default:
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
	 */
	@Override
	protected Point getSize() {
		return sizeOfImage;
	}

	/**
	 * Get the image formed by overlaying different images on the base image
	 * 
	 * @return composite image
	 */
	public Image getImage() {
		return createImage();
	}
}
