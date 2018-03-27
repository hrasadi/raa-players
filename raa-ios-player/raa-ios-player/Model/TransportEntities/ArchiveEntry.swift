//
//  ArchiveEntry.swift
//  raa-ios-player
//
//  Created by Hamid on 2/24/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation

class ArchiveEntry : Codable {
    var Program: CProgram?
    var ReleaseDateString: String?
}

extension ArchiveEntry : Playable {
    func getMediaPath() -> String? {
        return Program?.Show?.Clips?[0].Media?.Path
    }
    
    func getMediaLength() -> Double? {
        return Program?.Show?.Clips?[0].Media?.Duration
    }
    
    
}
