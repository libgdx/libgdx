/*
 The zlib/libpng License
 
 Copyright (c) 2005-2007 Phillip Castaneda (pjcast -- www.wreckedgames.com)
 
 This software is provided 'as-is', without any express or implied warranty. In no event will
 the authors be held liable for any damages arising from the use of this software.
 
 Permission is granted to anyone to use this software for any purpose, including commercial
 applications, and to alter it and redistribute it freely, subject to the following
 restrictions:
 
 1. The origin of this software must not be misrepresented; you must not claim that
 you wrote the original software. If you use this software in a product,
 an acknowledgment in the product documentation would be appreciated but is
 not required.
 
 2. Altered source versions must be plainly marked as such, and must not be
 misrepresented as being the original software.
 
 3. This notice may not be removed or altered from any source distribution.
 */
#include "mac/CocoaMouse.h"
#include "mac/CocoaInputManager.h"
#include "mac/CocoaHelpers.h"
#include "OISException.h"
#include "OISEvents.h"

using namespace OIS;

//-------------------------------------------------------------------//
CocoaMouse::CocoaMouse( InputManager* creator, bool buffered )
	: Mouse(creator->inputSystemName(), buffered, 0, creator)
{
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];

	CocoaInputManager *man = static_cast<CocoaInputManager*>(mCreator);
    mResponder = [[CocoaMouseView alloc] initWithFrame:[[man->_getWindow() contentView] frame]];
    if(!mResponder)
        OIS_EXCEPT( E_General, "CocoaMouseView::CocoaMouseView >> Error creating event responder" );
    
    [[man->_getWindow() contentView] addSubview:mResponder];
    [mResponder setOISMouseObj:this];
    
	static_cast<CocoaInputManager*>(mCreator)->_setMouseUsed(true);

    [pool drain];
}

CocoaMouse::~CocoaMouse()
{
	// Restore Mouse
//	CGAssociateMouseAndMouseCursorPosition(true);
	CGDisplayShowCursor(kCGDirectMainDisplay);

    if (mResponder)
    {
        [mResponder release];
        mResponder = nil;
    }
    
	static_cast<CocoaInputManager*>(mCreator)->_setMouseUsed(false);
}

void CocoaMouse::_initialize()
{
	mState.clear();
	CGAssociateMouseAndMouseCursorPosition(false);
}

void CocoaMouse::setBuffered( bool buffered )
{
	mBuffered = buffered;
}

void CocoaMouse::capture()
{
    [mResponder capture];
}

@implementation CocoaMouseView

- (id)initWithFrame:(NSRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        mTempState.clear();
        mMouseWarped = false;
        mNeedsToRegainFocus = false;
        
        // Hide OS Mouse
        CGDisplayHideCursor(kCGDirectMainDisplay);

        NSRect clipRect = NSMakeRect(0.0f, 0.0f, 0.0f, 0.0f);
        clipRect = [[[self window] contentView] frame];

        CGPoint warpPoint;
		warpPoint.x = (((frame.origin.x + frame.size.width) - frame.origin.x) / 2) + frame.origin.x;
		warpPoint.y = (((frame.origin.y + frame.size.height) - frame.origin.y) / 2) - frame.origin.y;
//        warpPoint = CGPointMake(clipRect.size.height, clipRect.size.width);
        CGDisplayMoveCursorToPoint(kCGDirectMainDisplay, warpPoint);

        // Use NSTrackingArea to track mouse move events
        NSTrackingAreaOptions trackingOptions = 
            NSTrackingMouseMoved | NSTrackingEnabledDuringMouseDrag | 
            NSTrackingMouseEnteredAndExited | NSTrackingActiveInActiveApp;
        
        NSDictionary *trackerData = [NSDictionary dictionaryWithObjectsAndKeys:
                                     [NSNumber numberWithInt:0], @"OISMouseTrackingKey", nil];
        NSTrackingArea *trackingArea = [[NSTrackingArea alloc]
                                        initWithRect:[self frame]// in our case track the entire view
                                        options:trackingOptions
                                        owner:self
                                        userInfo:trackerData];
        [self addTrackingArea:trackingArea];
        [[self window] setAcceptsMouseMovedEvents:YES];
        [trackingArea release];
    }
    return self;
}

- (BOOL)acceptsFirstMouse:(NSEvent *)theEvent
{
    return YES;
}

- (void)setOISMouseObj:(CocoaMouse *)obj
{
    oisMouseObj = obj;
}

- (void)capture
{
	MouseState *state = oisMouseObj->getMouseStatePtr();
    state->X.rel = 0;
    state->Y.rel = 0;
    state->Z.rel = 0;
    
	if(mTempState.X.rel || mTempState.Y.rel || mTempState.Z.rel)
	{
//		NSLog(@"%i %i %i", mTempState.X.rel, mTempState.Y.rel, mTempState.Z.rel);
        
		// Set new relative motion values
		state->X.rel = mTempState.X.rel;
		state->Y.rel = mTempState.Y.rel;
		state->Z.rel = mTempState.Z.rel;
		
		// Update absolute position
		state->X.abs += mTempState.X.rel;
		state->Y.abs += mTempState.Y.rel;
		
		if(state->X.abs > state->width)
			state->X.abs = state->width;
		else if(state->X.abs < 0)
			state->X.abs = 0;
        
		if(state->Y.abs > state->height)
			state->Y.abs = state->height;
		else if(state->Y.abs < 0)
			state->Y.abs = 0;
        
		state->Z.abs += mTempState.Z.rel;
		
		//Fire off event
        if ( oisMouseObj->buffered() && oisMouseObj->getEventCallback() )
			oisMouseObj->getEventCallback()->mouseMoved(MouseEvent(oisMouseObj, *state));
	}
    
	mTempState.clear();
}

#pragma mark Left Mouse Event overrides
- (void)mouseDown:(NSEvent *)theEvent
{
    int mouseButton = MB_Left;
    NSEventType type = [theEvent type];
    MouseState *state = oisMouseObj->getMouseStatePtr();

    if(mNeedsToRegainFocus)
        return;

    if((type == NSLeftMouseDown) && ([theEvent modifierFlags] & NSAlternateKeyMask))
    {
        mouseButton = MB_Middle;
    }
    else if((type == NSLeftMouseDown) && ([theEvent modifierFlags] & NSControlKeyMask))
    {
        mouseButton = MB_Right;
    }
    else if(type == NSLeftMouseDown)
    {
        mouseButton = MB_Left;
    }
    state->buttons |= 1 << mouseButton;
    if ( oisMouseObj->buffered() && oisMouseObj->getEventCallback() )
        oisMouseObj->getEventCallback()->mousePressed( MouseEvent( oisMouseObj, *state ), (MouseButtonID)mouseButton );
}

- (void)mouseUp:(NSEvent *)theEvent {
    int mouseButton = MB_Left;
    NSEventType type = [theEvent type];
    MouseState *state = oisMouseObj->getMouseStatePtr();

    if((type == NSLeftMouseUp) && ([theEvent modifierFlags] & NSAlternateKeyMask))
    {
        mouseButton = MB_Middle;
    }
    else if((type == NSLeftMouseUp) && ([theEvent modifierFlags] & NSControlKeyMask))
    {
        mouseButton = MB_Right;
    }
    else if(type == NSLeftMouseUp)
    {
        mouseButton = MB_Left;
    }
    state->buttons &= ~(1 << mouseButton);

    if ( oisMouseObj->buffered() && oisMouseObj->getEventCallback() )
        oisMouseObj->getEventCallback()->mouseReleased( MouseEvent( oisMouseObj, *state ), (MouseButtonID)mouseButton );
}


- (void)mouseDragged:(NSEvent *)theEvent
{
    CGPoint delta = CGPointMake([theEvent deltaX], [theEvent deltaY]);
    if(mNeedsToRegainFocus)
        return;
    
    // Relative positioning
    if(!mMouseWarped)
    {
        mTempState.X.rel += delta.x;
        mTempState.Y.rel += delta.y;
    }
    
    mMouseWarped = false;
}

#pragma mark Right Mouse Event overrides
- (void)rightMouseDown:(NSEvent *)theEvent
{
    int mouseButton = MB_Right;
    NSEventType type = [theEvent type];
    MouseState *state = oisMouseObj->getMouseStatePtr();
    
    if(mNeedsToRegainFocus)
        return;
    
    if(type == NSRightMouseDown)
    {	
        state->buttons |= 1 << mouseButton;
    }

    if ( oisMouseObj->buffered() && oisMouseObj->getEventCallback() )
        oisMouseObj->getEventCallback()->mousePressed( MouseEvent( oisMouseObj, *state ), (MouseButtonID)mouseButton );
}

- (void)rightMouseUp:(NSEvent *)theEvent {
    int mouseButton = MB_Right;
    NSEventType type = [theEvent type];
    MouseState *state = oisMouseObj->getMouseStatePtr();
    
    if(type == NSRightMouseUp)
    {	
        state->buttons &= ~(1 << mouseButton);
    }

    if ( oisMouseObj->buffered() && oisMouseObj->getEventCallback() )
        oisMouseObj->getEventCallback()->mouseReleased( MouseEvent( oisMouseObj, *state ), (MouseButtonID)mouseButton );
}

- (void)rightMouseDragged:(NSEvent *)theEvent
{
    CGPoint delta = CGPointMake([theEvent deltaX], [theEvent deltaY]);
    if(mNeedsToRegainFocus)
        return;
    
    // Relative positioning
    if(!mMouseWarped)
    {
        mTempState.X.rel += delta.x;
        mTempState.Y.rel += delta.y;
    }
    
    mMouseWarped = false;
}

#pragma mark Other Mouse Event overrides
- (void)otherMouseDown:(NSEvent *)theEvent
{
    int mouseButton = MB_Middle;
    NSEventType type = [theEvent type];
    MouseState *state = oisMouseObj->getMouseStatePtr();
    
    if(mNeedsToRegainFocus)
        return;
    
    if(type == NSOtherMouseDown)
    {
        state->buttons |= 1 << mouseButton;
    }

    if ( oisMouseObj->buffered() && oisMouseObj->getEventCallback() )
        oisMouseObj->getEventCallback()->mousePressed( MouseEvent( oisMouseObj, *state ), (MouseButtonID)mouseButton );
}

- (void)otherMouseUp:(NSEvent *)theEvent {
    int mouseButton = MB_Middle;
    NSEventType type = [theEvent type];
    MouseState *state = oisMouseObj->getMouseStatePtr();
    
    if(type == NSOtherMouseUp)
    {
        state->buttons &= ~(1 << mouseButton);
    }

    if ( oisMouseObj->buffered() && oisMouseObj->getEventCallback() )
        oisMouseObj->getEventCallback()->mouseReleased( MouseEvent( oisMouseObj, *state ), (MouseButtonID)mouseButton );
}

- (void)otherMouseDragged:(NSEvent *)theEvent
{
    CGPoint delta = CGPointMake([theEvent deltaX], [theEvent deltaY]);
    if(mNeedsToRegainFocus)
        return;
    
    // Relative positioning
    if(!mMouseWarped)
    {
        mTempState.X.rel += delta.x;
        mTempState.Y.rel += delta.y;
    }
    
    mMouseWarped = false;
}

- (void)scrollWheel:(NSEvent *)theEvent
{
    if([theEvent deltaY] != 0.0)
        mTempState.Z.rel += ([theEvent deltaY] * 60);
}

- (void)mouseMoved:(NSEvent *)theEvent
{
    CGPoint delta = CGPointMake([theEvent deltaX], [theEvent deltaY]);
    if(mNeedsToRegainFocus)
        return;
    
    // Relative positioning
    if(!mMouseWarped)
    {
        mTempState.X.rel += delta.x;
        mTempState.Y.rel += delta.y;
    }
    
    mMouseWarped = false;
}

- (void)mouseEntered:(NSEvent *)theEvent
{
	CGDisplayHideCursor(kCGDirectMainDisplay);
	CGAssociateMouseAndMouseCursorPosition(false);
    if(!mMouseWarped)
    {
        NSPoint pos = [[self window] mouseLocationOutsideOfEventStream];
        NSRect frame = [[[self window] contentView] frame];

        // Clear the previous mouse state
        MouseState *state = oisMouseObj->getMouseStatePtr();
        state->clear();

        // Cocoa's coordinate system has the origin in the bottom left so we need to transform the height
        mTempState.X.rel = pos.x;
        mTempState.Y.rel = frame.size.height - pos.y;
    }
}

- (void)mouseExited:(NSEvent *)theEvent
{
	CGDisplayShowCursor(kCGDirectMainDisplay);
	CGAssociateMouseAndMouseCursorPosition(true);
}

@end
