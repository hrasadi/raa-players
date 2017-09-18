//
//  PlaybackManager.swift
//  raa-ios-player
//
//  Created by Hamid on 9/14/17.
//  Copyright © 2017 Auto-asaad. All rights reserved.
//

import UIKit
import Foundation
import AVFoundation
import UserNotifications
import MediaPlayer

class PlaybackManager : NSObject {

    let audioSession = AVAudioSession.sharedInstance()
    let mpInfoCenter: MPNowPlayingInfoCenter = MPNowPlayingInfoCenter.default()

    var player: AVPlayer? = nil

    private var isCurrentlyPlaying: Bool?

    // When playback in progress
    private var currentBox: String?
    private var currentProgram: String?
    private var currentClip: String?
    
    // When playback not in progress
    private var nextBoxId: String?
    private var nextBoxStartTime: Date?
    
    private var activeTimer: Timer?
    private let dateFormatter = DateFormatter()
    private let coarseDateFormatter = DateFormatter()

    private var unsentProgramStartMessage: String?
    // Returns the new program name
    private var onProgramStartCallback: ((String) -> Void)?
    public var programStartCallback: ((String) -> Void)? {
        get {
            return onProgramStartCallback
        }
        set(callback) {
            onProgramStartCallback = callback
            if unsentProgramStartMessage != nil {
                onProgramStartCallback!(unsentProgramStartMessage!)
            }
        }
    }
    
    private var unsentPlaybackStopMessage: (nextBoxId: String?, nextBoxStartTime: Date?)?
    // Returns the upcoming box Id and date for start
    private var onPlaybackStopCallback: ((String?, Date?) -> Void)?
    public var playbackStopCallback: ((String?, Date?) -> Void)?  {
        get {
            return onPlaybackStopCallback
        }
        set(callback) {
            onPlaybackStopCallback = callback
            if unsentPlaybackStopMessage != nil {
                onPlaybackStopCallback!(unsentPlaybackStopMessage!.nextBoxId, unsentPlaybackStopMessage!.nextBoxStartTime)
            }
        }
    }
    
    let notificationCenter = UNUserNotificationCenter.current()

    override init() {
        super.init()
                
        dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ssZZZZ"
        coarseDateFormatter.dateFormat = "yyyy-MM-dd"
        
        // Register notification categories and actions
        let listenAction = UNNotificationAction(identifier: "LISTEN_ACTION",
                                                title: "گوش می‌دهم",
                                                options: UNNotificationActionOptions(rawValue: 0))

        let generalCategory = UNNotificationCategory(identifier: "GENERAL",
                                                     actions: [listenAction],
                                                     intentIdentifiers: [],
                                                     options: .customDismissAction)
        
        // Register the notification category
        notificationCenter.setNotificationCategories([generalCategory])
        
        // And also configure AudioSession to show on control center
        do {
            try audioSession.setCategory(AVAudioSessionCategoryPlayback, with: [AVAudioSessionCategoryOptions.allowBluetooth])
        } catch let e {
            print("Error while configuring AudioSession. Error is: " + e.localizedDescription)
        }
    }
    
    func registerNotificationDelegate() {        
        UNUserNotificationCenter.current().delegate = self
    }
    
    /*
     IMPLEMENTATION NOTE: In some cases, the activate func is called, without prior deactivate. 
                        Example is when user opens the control center drawer on top of the active app and then closes it. iOS will call activate only on drawer closing. We need to handle this case also
     */
    func activate() {
        
        // This might not the case if playback has been started by clicking notification
        if (player == nil) {
            self.play()
        } 
        
        // Run once
        loadStatus()
        
        if (activeTimer == nil) {
            activeTimer = Timer.scheduledTimer(timeInterval: 10, target: self, selector: #selector(self.loadStatus), userInfo: nil, repeats: true)
        } else {
            // There is already an active time, so do not do anything
            print("Have already an active timer. Skipping timer instantiation")
        }
    }
    
    func deactivate() {
        // Stop the player and unpopulate the media info center unless user allow us to it
        // Also, if nothing is playing, there is not need to continue playback in background
        if (!(Settings.getValue(Settings.BackgroundPlayKey) ?? false) || !(isCurrentlyPlaying ?? false)) {
            self.stop()
            unpopulateMediaInfoCenterNowPlaying()
        }
        
        activeTimer?.invalidate()
        activeTimer = nil
    }
    
    func play() {
        player = AVPlayer.init(url: URL.init(string: "https://stream.raa.media/raa1.ogg")!)
        // Always play while active
        player?.play()
        
        populateMediaInfoCenterNowPlaying()
    }
    
    func stop(unpopulateMediaInfoCenter: Bool = false) {
        // Deactivate player when going inactive (we must re-activate if we are supposed to playback in backgound)
        player?.pause()
        player = nil
    }
    
    func populateMediaInfoCenterNowPlaying() {
        let image = UIImage(named: "raa-logo-256.png")!
        if (currentProgram != nil && currentClip != nil) {
            mpInfoCenter.nowPlayingInfo = [MPMediaItemPropertyAlbumTitle: currentClip!,
                                           MPMediaItemPropertyTitle: currentProgram!,
                                           MPMediaItemPropertyArtist: "رادیو اتو-اسعد",
                                           MPMediaItemPropertyArtwork: MPMediaItemArtwork(boundsSize: image.size) { sz in return image
                                            },
                                           MPNowPlayingInfoPropertyPlaybackRate: player?.rate ?? 0]
            
            // listen info center events
            UIApplication.shared.beginReceivingRemoteControlEvents()
        }
    }
    
    func unpopulateMediaInfoCenterNowPlaying() {
        mpInfoCenter.nowPlayingInfo = nil
    }

    @objc func loadStatus(_ sendNotification: Bool = false) {
        if let data = try? Data.init(contentsOf: URL(string: "http://raa.media/lineups/status.json")!) {
            let status = try? JSONSerialization.jsonObject(with: data, options: []) as! Dictionary<String, Any>
            
            var isCurrentlyPlaying_new = status?["isCurrentlyPlaying"] as? Bool ?? false

            var currentBox_new = status?["currentBox"] as? String
            let currentProgram_new = status?["currentProgram"] as? String
            let currentClip_new = status?["currentClip"] as? String

            let nextBoxId_new = status?["nextBoxId"] as? String
            var nextBoxStartTime_new: Date?;
            if let nextBoxStartTimeString = status?["nextBoxStartTime"] as? String {
                nextBoxStartTime_new = dateFormatter.date(from: nextBoxStartTimeString)
            }
            
            // Before moving on, we decide whether or not a new program was started since last status
            if isCurrentlyPlaying_new {
                // Is this a new program?
                // Note that we also cover the transition of playback start by knowing the fact that 'currentProgram' in the case of no program is 'BLANK'
                if currentProgram_new != currentProgram || currentBox_new != currentBox {
                    // Update player bar in foreground mode
                    if onProgramStartCallback != nil {
                        onProgramStartCallback!(currentProgram_new!)
                    } else {
                       unsentProgramStartMessage = currentProgram_new
                    }

                    // Do notify if in backgroud
                    if (sendNotification) {
                        createNotification(currentProgram_new!, programBox: currentBox_new!)
                    }
                }
            } else if !isCurrentlyPlaying_new && isCurrentlyPlaying_new != isCurrentlyPlaying {
                // If playback stopped
                // Update player bar
                if onPlaybackStopCallback != nil {
                   onPlaybackStopCallback!(nextBoxId_new, nextBoxStartTime_new)
                } else {
                    unsentPlaybackStopMessage = (nextBoxId: nextBoxId_new, nextBoxStartTime: nextBoxStartTime_new)
                }
            }

            // Update state
            isCurrentlyPlaying = isCurrentlyPlaying_new
            currentBox = currentBox_new
            currentProgram = currentProgram_new
            currentClip = currentClip_new
            nextBoxId = nextBoxId_new
            nextBoxStartTime = nextBoxStartTime_new
            
            populateMediaInfoCenterNowPlaying()
        }
    }
    
    func createNotification(_ programName: String, programBox: String) {
        let content = UNMutableNotificationContent()
        content.body = NSString.localizedUserNotificationString(forKey: "در حال پخش: " + programName, arguments: nil)

        // We only offer 'immediate listening' only in cases that they allow us to perform background playback
        if (Settings.getValue(Settings.BackgroundPlayKey) ?? false) {
            content.categoryIdentifier = "GENERAL"
        }
        
        let notificationId = coarseDateFormatter.string(from: Date()) + "-" + programBox + "-" + programName
        let noficationRequest = UNNotificationRequest(identifier: notificationId, content: content, trigger: UNTimeIntervalNotificationTrigger(timeInterval: 1.0, repeats: false))
        
        notificationCenter.add(noficationRequest, withCompletionHandler: { (error) in
            if error != nil {
                // Something went wrong
                print("error while submitting notification")
            }
        })
    }
    
}

extension PlaybackManager: UNUserNotificationCenterDelegate {
    func userNotificationCenter(_: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        let actionIdentifier = response.actionIdentifier

        switch actionIdentifier {
        case "LISTEN_ACTION":
            do {
                try audioSession.setActive(true)
                self.play()
            } catch let e {
                print("Error happened while starting playback: " + e.localizedDescription)
            }
            
        default:
            break;
        }
        completionHandler()
   
    }
}
