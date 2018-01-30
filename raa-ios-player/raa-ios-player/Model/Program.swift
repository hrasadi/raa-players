//
//  Program.swift
//  raa-ios-player
//
//  Created by Hamid on 1/28/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation

class Program : NSObject {
    required override init() {
        super.init()
    }
    
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

class PersonalProgram : Program {
    
}

class PublicProgram : Program {
    
}

class LiveProgram : Program {
    
}
