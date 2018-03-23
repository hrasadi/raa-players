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
    @IBOutlet weak var itemTitle: UILabel!
    @IBOutlet weak var itemSubtitle: UILabel!
    @IBOutlet weak var itemThumbnail: UIImageView!
    @IBOutlet weak var playbackState: UIButton!
    @IBOutlet weak var cancelPlayback: UIButton!
    
    public var delegate: PlayerViewDelegate?
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
                
        // XIB file, please be silent and behave to the constraint your superview tell you :)
        self.translatesAutoresizingMaskIntoConstraints = false
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
    
    @IBAction func playButtonClicked(_ sender: Any) {
        self.delegate?.onPlayPauseButtonClicked()
    }
    
    @IBAction func cancelButtonClicked(_ sender: Any) {
        self.delegate?.onCancelButtonClicked()
    }
}

protocol PlayerViewDelegate {
    func onPlayPauseButtonClicked()
    func onCancelButtonClicked()
}

