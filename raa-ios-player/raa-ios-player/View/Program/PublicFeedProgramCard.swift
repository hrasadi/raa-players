//
//  PublicFeedProgramCard.swift
//  raa-ios-player
//
//  Created by Hamid on 2/1/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit

@IBDesignable
class PublicFeedProgramCard : ProgramCard {
    public var feedEntryId: String?

    public var feedDelegate: FeedCardDelegate?
    
    @IBInspectable private var buttonText = "پخش"
    public var actionBtn = UIButton()

    private var btnBackgroundColor = UIColor(red: 47/255, green: 133/255, blue: 116/255, alpha: 1)
    
    override public init(frame: CGRect) {
        super.init(frame: frame)
        initialize()
    }
    
    required public init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        initialize()
    }

    override func initialize() {
        super.initialize()

        self.actionBtn.addTarget(self, action: #selector(actionButtonTapped), for: UIControlEvents.touchUpInside)
        self.actionBtn.addTarget(self, action: #selector(actionButtonTouchDown), for: UIControlEvents.touchDown)

        self.timeTitle1 = "انتشار"
        self.timeTitle2 = "انقضا"
        
        backgroundIV.addSubview(actionBtn)
        
    }
    
    override open func draw(_ rect: CGRect) {
        super.draw(rect)
        
        actionBtn.backgroundColor = UIColor.clear
        actionBtn.layer.backgroundColor = btnBackgroundColor.cgColor
        actionBtn.clipsToBounds = true
        let btnTitle = NSAttributedString(string: buttonText.uppercased(), attributes: [ NSAttributedStringKey.font : UIFont.systemFont(ofSize: 16, weight: .medium), NSAttributedStringKey.foregroundColor : UIColor.white])
        actionBtn.setAttributedTitle(btnTitle, for: .normal)
        
        layout()
    }
    
    override func layout(animating: Bool = true) {
        super.layout(animating: animating)
        
        let gimme = LayoutHelper(rect: backgroundIV.bounds)

        actionBtn.frame = CGRect(x: blurV.bounds.width,
                                 y: gimme.RevY(0, height: 32),
                                 width: backgroundIV.bounds.width - blurV.bounds.width,
                                 height: 32)
    }
    
    @objc func actionButtonTouchDown() {
        self.actionBtn.isHighlighted = true
    }

    @objc func actionButtonTapped() {
        self.actionBtn.isHighlighted = false
        
        UIView.animate(withDuration: 0.1, animations: {
            self.actionBtn.backgroundColor = UIColor.black
        }) { _ in
            UIView.animate(withDuration: 0.1, animations: {
                self.actionBtn.backgroundColor = self.btnBackgroundColor
            })
        }

        
        if self.feedDelegate != nil {
            self.feedDelegate!.onPlayButtonClicked(self.feedEntryId ?? "")
        }
    }
}

class PublicFeedProgramCardTableViewCell : UITableViewCell {
    @IBOutlet public var card: PublicFeedProgramCard?
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override init(style aStyle: UITableViewCellStyle, reuseIdentifier rid: String?) {
        super.init(style: aStyle, reuseIdentifier: rid)
    }
}

protocol FeedCardDelegate {
    func onPlayButtonClicked(_ requestedFeedEntryId: String)
}

