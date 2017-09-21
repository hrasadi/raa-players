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
    
    // Indicates the system authorization
    public static var authorizedToSendNotification = true
    public static var backgroundTask: UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid
    
    private var settings: UserDefaults
    private var playbackManager: PlaybackManager = PlaybackManager()
    private var notificationManager: NotificationManager = NotificationManager()

    private var currentLineup: Dictionary<String, Any>?

    let persianNumberFormatter: NumberFormatter = NumberFormatter()
    
    private static let instance = Settings()
    
    private init() {
        settings = UserDefaults.standard
        // Populate default settings (Yes to all features!)
        if (settings.object(forKey: Settings.BackgroundPlayKey) == nil) {
            settings.set(true, forKey: Settings.BackgroundPlayKey)
        }
        if (settings.object(forKey: Settings.NotifyNewProgramKey) == nil) {
            settings.set(true, forKey: Settings.NotifyNewProgramKey)
        }
        
        persianNumberFormatter.locale = Locale(identifier: "fa_IR")
    }
    
    class func startup() -> Settings {
        return instance
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
    
    class func getNotificationManager() -> NotificationManager {
        return instance.notificationManager
    }
    
    class func convertToPersianLocaleString(_ str: String?) -> String? {
        if (str == nil) {
            return nil;
        }
        var result: String = str!
        for i in 0...9 {
            result = result.replacingOccurrences(of: String(i), with: instance.persianNumberFormatter.string(from: NSNumber(value: i))!)
        }        
        return result
    }

}
