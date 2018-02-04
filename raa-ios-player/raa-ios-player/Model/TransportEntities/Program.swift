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
}

class PersonalProgram : CProgram {
    
}

class PublicFeedEntry : Codable {
    var Id: String! {
        didSet {
            if Id == nil {
                Id = ""
            }
        }
    }

    var Program: String? {
        didSet {
            self.ProgramObject = try! JSONDecoder().decode(CProgram.self, from: (self.Program?.data(using: String.Encoding.utf8))!)
        }
    }
    var ProgramObject: CProgram?
    
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

class LiveProgram : CProgram {
    
}
