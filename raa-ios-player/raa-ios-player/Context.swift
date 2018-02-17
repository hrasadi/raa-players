//
//  Context.swift
//  raa-ios-player
//
//  Created by Hamid on 1/28/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import CoreLocation


class Context {
    init() {
    }

    private static var instance: Context? = nil;
    public static var Instance: Context! {
        get {
            if instance == nil {
                instance = Context()
            }
            return instance!
        }
    }
    
    public static let API_URL_PREFIX = "http://api.raa.media:7800"
    public static let LIVE_INFO_URL_PREFIX = "https://raa.media/live"
    public static let LIVE_STREAM_URL_PREFIX = "https://stream.raa.media/raa1.ogg"

    public static func initiateManagers() {
        if instance == nil {
            instance = Context()
            
            instance?.userManager = UserManager()
            instance?.programInfoDirectoryManager = ProgramInfoDirectoryManager()
            instance?.feedManager = FeedManager()
            instance?.liveBroadcastManager = LiveBroadcastManager()
            instance?.playbackManager = PlaybackManager()
            
            // additional initiate functions
            instance?.userManager.initiate()
            instance?.feedManager.initiate()
            instance?.liveBroadcastManager.initiate()
        }
    }
        
    public var userManager: UserManager!
    public var programInfoDirectoryManager: ProgramInfoDirectoryManager!
    public var feedManager: FeedManager!
    public var liveBroadcastManager: LiveBroadcastManager!
    public var playbackManager: PlaybackManager!
    public let settings = UserDefaults.standard
}
