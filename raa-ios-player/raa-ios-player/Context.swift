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
                initiateManagers()
            }
            return instance!
        }
    }
    
    private static var initiated = false
    public static func isInitiated() -> Bool {
        return initiated
    }
    
    public static let BASE_URL_PREFIX = "https://raa.media"
    public static let API_URL_PREFIX = "http://api.raa.media:7800"
    public static let LIVE_STREAM_URL_PREFIX = "https://stream.raa.media"
    public static let ARCHIVE_URL_PREFIX = Context.BASE_URL_PREFIX + "/archive"
    public static let RSS_URL_PREFIX = Context.BASE_URL_PREFIX + "/rss"
    public static let LIVE_INFO_URL_PREFIX = Context.BASE_URL_PREFIX + "/live"

    public static func initiateManagers() {
        if instance == nil {
            instance = Context()
            
            instance?.userManager = UserManager()
            instance?.programInfoDirectoryManager = ProgramInfoDirectoryManager()
            instance?.feedManager = FeedManager()
//            instance?.liveBroadcastManager = LiveBroadcastManager()
            instance?.archiveManager = ArchiveManager()
            instance?.playbackManager = PlaybackManager()
            
            // additional initiate functions
            instance?.userManager.initiate()
            instance?.programInfoDirectoryManager.initiate()
            instance?.feedManager.initiate()
//            instance?.liveBroadcastManager.initiate()
            instance?.archiveManager.initiate()
            instance?.playbackManager.initiate()
            
            initiated = true
        }
    }
    
    public func reloadLineups() {
//        self.liveBroadcastManager.initiate()
        self.feedManager.initiate()
    }
        
    public var userManager: UserManager!
    public var programInfoDirectoryManager: ProgramInfoDirectoryManager!
    public var feedManager: FeedManager!
    public var liveBroadcastManager: LiveBroadcastManager!
    public var archiveManager: ArchiveManager!
    public var playbackManager: PlaybackManager!
    public let settings = UserDefaults.standard
    
    // This will be true on first time app opens
    // Use for tutorial, custom messages, etc.
    public var isFirstExecution = false
}
