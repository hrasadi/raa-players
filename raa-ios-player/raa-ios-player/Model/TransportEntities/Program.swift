//
//  Program.swift
//  raa-ios-player
//
//  Created by Hamid on 1/28/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation

class CProgram : Codable {
    var ProgramId: String! = ""
    var CanonicalIdPath: String! = ""
    var Title: String?
    var Subtitle: String?
    var Show: CShow?
    var Metadata: CMetadata?
    
    class CShow : Codable {
        var Clips: [CClip]?
        
        class CClip : Codable {
            var Media: CMedia?
            
            class CMedia : Codable {
                var Path: String?
            }
        }
    }
    class CMetadata : Codable {
        var StartTime: String?
        var EndTime: String?
    }
}

