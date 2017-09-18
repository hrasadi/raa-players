//
//  Settings.swift
//  raa-ios-player
//
//  Created by Hamid on 9/13/17.
//  Copyright © 2017 Auto-asaad. All rights reserved.
//

import Foundation
import UserNotificationsUI
import UserNotifications

class Settings {
    public static let BackgroundPlayKey = "backgroundPlay"
    public static let NotifyNewProgramKey = "notifyNewProgram"
    
    private var settings: UserDefaults
    private var playbackManager: PlaybackManager
    
    // Indicates the system authorization
    static var authorizedToSendNotification = true
    
    private var currentLineup: Dictionary<String, Any>?
    
    private static let instance = Settings()
    
    private init() {
        settings = UserDefaults.standard
        playbackManager = PlaybackManager()
    }
    
    class func getValue(_ key: String) -> Bool? {
        return instance.settings.bool(forKey: key)
    }
    
    class func setValue(_ key: String, newValue val: Bool) {
        instance.settings.set(val, forKey: key)
    }
    
    class func loadLineup() {
        let date = Date()
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "YYYY-MM-dd"
        
        let lineupFileUrlString = "http://raa.media/lineups/lineup-" + dateFormatter.string(from: date) + ".json"
        
        if let data = try? Data.init(contentsOf: URL(string: lineupFileUrlString)!) {
            instance.currentLineup = try? JSONSerialization.jsonObject(with: data, options: []) as! Dictionary<String, Any>
        }
    }
    
    class func getLineup() -> Dictionary<String, Any>? {
        return instance.currentLineup;
    }

    class func getPlaybackManager() -> PlaybackManager {
        return instance.playbackManager;
    }

}
