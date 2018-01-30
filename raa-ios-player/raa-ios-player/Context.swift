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
    
    public static func initiate() {
        if instance == nil {
            instance = Context()
            
            instance?.userManager = UserManager()
        }
    }
        
    public let settings = UserDefaults.standard
    public var userManager: UserManager!
    
    // TODO: Move these to respective classes
    private struct PropertyKey {
        // Config from settings page
        static let BackgroundPlayback = "backgroundPlayback"
        static let PersonalProgramPushNotification = "personalProgramPushNotification"
        static let LiveProgramPushNotification = "liveProgramPushNotification"
    }
    

}
