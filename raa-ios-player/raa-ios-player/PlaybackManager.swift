//
//  Player.swift
//  raa-ios-player
//
//  Created by Hamid on 9/14/17.
//  Copyright © 2017 Auto-asaad. All rights reserved.
//

import Foundation

class PlaybackManager {

    private var status: Dictionary<String, Any>?

    private var isCurrentlyPlaying: Bool = false

    // When playback in progress
    private var currentBox: String?
    private var currentProgram: String?
    private var currentClip: String?
    
    // When playback not in progress
    private var nextBoxId: String?
    private var nextBoxStartTime: Date?
    
    private var activeTimer: Timer?
    private let dateFormatter = DateFormatter()

    init() {
        dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ssZZZZ"
    }
    
    func activateTimer() {
        activeTimer = Timer.scheduledTimer(timeInterval: 10, target: self, selector: #selector(self.loadStatus), userInfo: nil, repeats: true)
    }
    
    func deactivateTimer() {
        activeTimer?.invalidate()
        activeTimer = nil
    }
    
    @objc func loadStatus(_ sendNotification: Bool = false) {
        if let data = try? Data.init(contentsOf: URL(string: "http://raa.media/lineups/status.json")!) {
            let status = try? JSONSerialization.jsonObject(with: data, options: []) as! Dictionary<String, Any>
            
            decidePlaybackStatus();
            
            isCurrentlyPlaying = status?["isCurrentlyPlaying"] as? Bool ?? false

            currentBox = status?["currentBox"] as? String
            currentProgram = status?["currentProgram"] as? String
            currentClip = status?["currentClip"] as? String

            nextBoxId = status?["nextBoxId"] as? String
            if let nextBoxStartTimeString = status?["nextBoxStartTime"] as? String {
                nextBoxStartTime = dateFormatter.date(from: nextBoxStartTimeString)
            }
        }
    }

    func decidePlaybackStatus() {
        if isCurrentlyPlaying {
            
            // App is in backgroud, send a notification to le
//            if (sendNotification) {
//                
//            }
        }
        
    }
}
