//
//  PlayerView.swift
//  raa-ios-player
//
//  Created by Hamid on 1/24/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit

@IBDesignable
class PlayerView : UIView {
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        drawBorder()
    }
    
    func drawBorder() {
        let lineView: UIView = UIView.init(frame: CGRect(x: 0, y: 0, width: self.frame.size.width, height: 1))
        lineView.backgroundColor = UIColor.lightGray
        
        self.addSubview(lineView)
        self.sendSubview(toBack: lineView)
    }
}
