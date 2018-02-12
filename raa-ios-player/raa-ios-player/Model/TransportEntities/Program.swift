//
//  Program.swift
//  raa-ios-player
//
//  Created by Hamid on 1/28/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation

class CProgram : Codable {
    
    var ProgramId: String! {
        didSet {
            if ProgramId == nil {
                ProgramId = ""
            }
        }
    }
    
    var Title: String! {
        didSet {
            if Title == nil {
                Title = ""
            }
        }
    }
    
    var StartTime: Date?
    var EndTime: Date?

    var Show: CShow?
    
    class CShow : Codable {
        
        var Clips: [CClip]?
        
        class CClip : Codable {
            
            var Media: CMedia?
            
            class CMedia : Codable {
                var Path: String?
            }
        }
    }
}

class LiveProgram : CProgram {
    
}

class PersonalFeedEntry : Codable {
    
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
