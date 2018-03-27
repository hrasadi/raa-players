//
//  Playable.swift
//  raa-ios-player
//
//  Created by Hamid on 3/25/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation

public protocol Playable {
    func getMediaPath() -> String?
    func getMediaLength() -> Double?
}
