//
//  FeedEntry.swift
//  raa-ios-player
//
//  Created by Hamid on 2/13/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation

// Swift Codable has many problems when it comes to inheritence management. This is giving me a very hard time copying my code everywhere
class PersonalFeedEntry : Codable {
    var Id: String! {
        didSet {
            if Id == nil {
                Id = ""
            }
        }
    }
    var Program: String?
    var ProgramObject: CProgram? {
        return try! JSONDecoder().decode(CProgram.self, from: (self.Program?.data(using: String.Encoding.utf8))!)
    }

    var ReleaseTimestamp: Double?
    var ExpirationTimestamp: Double?
}

class PublicFeedEntry : Codable {
    var Id: String! {
        didSet {
            if Id == nil {
                Id = ""
            }
        }
    }
    var Program: String?
    var ProgramObject: CProgram? {
        return try! JSONDecoder().decode(CProgram.self, from: (self.Program?.data(using: String.Encoding.utf8))!)
    }
    var Upvotes: Int! {
        didSet {
            if Upvotes == nil {
                Upvotes = 0
            }
        }
    }
    var ReleaseTimestamp: Double?
    var ExpirationTimestamp: Double?
}

extension PublicFeedEntry : Playable {
    func getMediaPath() -> String? {
        return ProgramObject?.Show?.Clips?[0].Media?.Path
    }
    
    func getMediaLength() -> Double? {
        return ProgramObject?.Show?.Clips?[0].Media?.Duration
    }
}
